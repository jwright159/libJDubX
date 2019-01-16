package wrightway.gdx;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.files.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.utils.XmlReader.*;
import com.badlogic.gdx.maps.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.maps.tiled.tiles.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.maps.objects.*;
import com.badlogic.gdx.maps.tiled.objects.*;

abstract public class TiledMapTileSetLoader{
	private static final boolean flipY = true;
	private static final int firstgid = 1;
	
	protected static final int FLAG_FLIP_HORIZONTALLY = 0x80000000;
	protected static final int FLAG_FLIP_VERTICALLY = 0x40000000;
	protected static final int FLAG_FLIP_DIAGONALLY = 0x20000000;
	protected static final int MASK_CLEAR = 0xE0000000;
	
	public static TiledMapTileSet loadTileSet(FileHandle file, TiledMap map){
		XmlReader xml = new XmlReader();
		Element element = xml.parse(file);

		String name = element.get("name", null);
		int tilewidth = element.getIntAttribute("tilewidth", 0);
		int tileheight = element.getIntAttribute("tileheight", 0);
		int spacing = element.getIntAttribute("spacing", 0);
		int margin = element.getIntAttribute("margin", 0);
		Element offset = element.getChildByName("tileoffset");
		int offsetX = 0, offsetY = 0;
		if(offset != null){
			offsetX = offset.getIntAttribute("x", 0);
			offsetY = offset.getIntAttribute("y", 0);
		}
		Element imageElement = element.getChildByName("image");

		String imageSource = "";
		int imageWidth = 0, imageHeight = 0;
		FileHandle image = null;
		if(imageElement != null){
			imageSource = imageElement.getAttribute("source");
			imageWidth = imageElement.getIntAttribute("width", 0);
			imageHeight = imageElement.getIntAttribute("height", 0);
			image = Utils.getRelativeFileHandle(file, imageSource);
		}

		TiledMapTileSet tileset = new TiledMapTileSet();
		tileset.setName(name);
		//tileset.getProperties().put("firstgid", firstgid);//for from map
		if(image != null){
			TextureRegion texture = new TextureRegion(new Texture(image));

			MapProperties props = tileset.getProperties();
			props.put("imagesource", imageSource);
			props.put("imagewidth", imageWidth);
			props.put("imageheight", imageHeight);
			props.put("tilewidth", tilewidth);
			props.put("tileheight", tileheight);
			props.put("margin", margin);
			props.put("spacing", spacing);

			int stopWidth = texture.getRegionWidth() - tilewidth;
			int stopHeight = texture.getRegionHeight() - tileheight;

			int id = firstgid;

			for(int y = margin; y <= stopHeight; y += tileheight + spacing){
				for(int x = margin; x <= stopWidth; x += tilewidth + spacing){
					TextureRegion tileRegion = new TextureRegion(texture, x, y, tilewidth, tileheight);
					TiledMapTile tile = new StaticTiledMapTile(tileRegion);
					tile.setId(id);
					tile.setOffsetX(offsetX);
					tile.setOffsetY(flipY ? -offsetY : offsetY);
					tileset.putTile(id++, tile);
				}
			}
		}else{
			Array<Element> tileElements = element.getChildrenByName("tile");
			for(Element tileElement : tileElements){
				imageElement = tileElement.getChildByName("image");
				if(imageElement != null){
					imageSource = imageElement.getAttribute("source");
					imageWidth = imageElement.getIntAttribute("width", 0);
					imageHeight = imageElement.getIntAttribute("height", 0);
					image = Utils.getRelativeFileHandle(file, imageSource);
				}
				TextureRegion texture = new TextureRegion(new Texture(image));
				TiledMapTile tile = new StaticTiledMapTile(texture);
				tile.setId(firstgid + tileElement.getIntAttribute("id"));
				tile.setOffsetX(offsetX);
				tile.setOffsetY(flipY ? -offsetY : offsetY);
				tileset.putTile(tile.getId(), tile);
			}
		}
		Array<Element> tileElements = element.getChildrenByName("tile");

		Array<AnimatedTiledMapTile> animatedTiles = new Array<AnimatedTiledMapTile>();

		for(Element tileElement : tileElements){
			int localtid = tileElement.getIntAttribute("id", 0);
			TiledMapTile tile = tileset.getTile(firstgid + localtid);
			if(tile != null){
				Element animationElement = tileElement.getChildByName("animation");
				if(animationElement != null){

					Array<StaticTiledMapTile> staticTiles = new Array<StaticTiledMapTile>();
					IntArray intervals = new IntArray();
					for(Element frameElement: animationElement.getChildrenByName("frame")){
						staticTiles.add((StaticTiledMapTile) tileset.getTile(firstgid + frameElement.getIntAttribute("tileid")));
						intervals.add(frameElement.getIntAttribute("duration"));
					}

					AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(intervals, staticTiles);
					animatedTile.setId(tile.getId());
					animatedTiles.add(animatedTile);
					tile = animatedTile;
				}

				Element objectgroupElement = tileElement.getChildByName("objectgroup");
				if(objectgroupElement != null){

					for(Element objectElement: objectgroupElement.getChildrenByName("object")){
						tile.getObjects().add(loadObject(objectElement, tile.getTextureRegion().getRegionHeight(), map));
					}
				}

				String terrain = tileElement.getAttribute("terrain", null);
				if(terrain != null){
					tile.getProperties().put("terrain", terrain);
				}
				String probability = tileElement.getAttribute("probability", null);
				if(probability != null){
					tile.getProperties().put("probability", probability);
				}
				Element properties = tileElement.getChildByName("properties");
				if(properties != null){
					loadProperties(tile.getProperties(), properties);
				}
			}
		}

		for(AnimatedTiledMapTile tile : animatedTiles){
			tileset.putTile(tile.getId(), tile);
		}

		Element properties = element.getChildByName("properties");
		if(properties != null){
			loadProperties(tileset.getProperties(), properties);
		}
		return tileset;
	}

	public static MapObject loadObject(Element element, int heightInPixels, TiledMap map){
		if(element.getName().equals("object")){
			MapObject object = null;

			float scaleX = 1;//convertObjectToTileSpace ? 1.0f / mapTileWidth : 1.0f;
			float scaleY = 1;//convertObjectToTileSpace ? 1.0f / mapTileHeight : 1.0f;

			float x = element.getFloatAttribute("x", 0) * scaleX;
			float y = (flipY ? heightInPixels - element.getFloatAttribute("y", 0) : element.getFloatAttribute("y", 0)) * scaleY;

			float width = element.getFloatAttribute("width", 0) * scaleX;
			float height = element.getFloatAttribute("height", 0) * scaleY;

			if(element.getChildCount() > 0){
				Element child = null;
				if((child = element.getChildByName("polygon")) != null){
					String[] points = child.getAttribute("points").split(" ");
					float[] vertices = new float[points.length * 2];
					for(int i = 0; i < points.length; i++){
						String[] point = points[i].split(",");
						vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
						vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
					}
					Polygon polygon = new Polygon(vertices);
					polygon.setPosition(x, y);
					object = new PolygonMapObject(polygon);
				}else if((child = element.getChildByName("polyline")) != null){
					String[] points = child.getAttribute("points").split(" ");
					float[] vertices = new float[points.length * 2];
					for(int i = 0; i < points.length; i++){
						String[] point = points[i].split(",");
						vertices[i * 2] = Float.parseFloat(point[0]) * scaleX;
						vertices[i * 2 + 1] = Float.parseFloat(point[1]) * scaleY * (flipY ? -1 : 1);
					}
					Polyline polyline = new Polyline(vertices);
					polyline.setPosition(x, y);
					object = new PolylineMapObject(polyline);
				}else if((child = element.getChildByName("ellipse")) != null){
					object = new EllipseMapObject(x, flipY ? y - height : y, width, height);
				}
			}
			if(object == null){
				String gid = null;
				if((gid = element.getAttribute("gid", null)) != null && map != null){
					int id = (int)Long.parseLong(gid);
					boolean flipHorizontally = ((id & FLAG_FLIP_HORIZONTALLY) != 0);
					boolean flipVertically = ((id & FLAG_FLIP_VERTICALLY) != 0);

					TiledMapTile tile = map.getTileSets().getTile(id & ~MASK_CLEAR);
					TiledMapTileMapObject tiledMapTileMapObject = new TiledMapTileMapObject(tile, flipHorizontally, flipVertically);
					TextureRegion textureRegion = tiledMapTileMapObject.getTextureRegion();
					tiledMapTileMapObject.getProperties().put("gid", id);
					tiledMapTileMapObject.setX(x);
					tiledMapTileMapObject.setY(flipY ? y : y - height);
					float objectWidth = element.getFloatAttribute("width", textureRegion.getRegionWidth());
					float objectHeight = element.getFloatAttribute("height", textureRegion.getRegionHeight());
					tiledMapTileMapObject.setScaleX(scaleX * (objectWidth / textureRegion.getRegionWidth()));
					tiledMapTileMapObject.setScaleY(scaleY * (objectHeight / textureRegion.getRegionHeight()));
					tiledMapTileMapObject.setRotation(element.getFloatAttribute("rotation", 0));
					object = tiledMapTileMapObject;
				}else{
					object = new RectangleMapObject(x, flipY ? y - height : y, width, height);
				}
			}
			object.setName(element.getAttribute("name", null));
			String rotation = element.getAttribute("rotation", null);
			if(rotation != null){
				object.getProperties().put("rotation", Float.parseFloat(rotation));
			}
			String type = element.getAttribute("type", null);
			if(type != null){
				object.getProperties().put("type", type);
			}
			int id = element.getIntAttribute("id", 0);
			if(id != 0){
				object.getProperties().put("id", id);
			}
			object.getProperties().put("x", x);

			if(object instanceof TiledMapTileMapObject){
				object.getProperties().put("y", y);
			}else{
				object.getProperties().put("y", (flipY ? y - height : y));
			}
			object.getProperties().put("width", width);
			object.getProperties().put("height", height);
			object.setVisible(element.getIntAttribute("visible", 1) == 1);
			Element properties = element.getChildByName("properties");
			if(properties != null){
				loadProperties(object.getProperties(), properties);
			}
			return object;
		}
		return null;
	}

	public static void loadProperties(MapProperties properties, Element element){
		if(element == null) return;
		if(element.getName().equals("properties")){
			for(Element property : element.getChildrenByName("property")){
				String name = property.getAttribute("name", null);
				String value = property.getAttribute("value", null);
				String type = property.getAttribute("type", null);
				if(value == null){
					value = property.getText();
				}
				Object castValue = castProperty(name, value, type);
				properties.put(name, castValue);
			}
		}
	}

	private static Object castProperty(String name, String value, String type){
		if(type == null){
			return value;
		}else if(type.equals("int")){
			return Integer.valueOf(value);
		}else if(type.equals("float")){
			return Float.valueOf(value);
		}else if(type.equals("bool")){
			return Boolean.valueOf(value);
		}else if(type.equals("color")){
			// Tiled uses the format #AARRGGBB
			String opaqueColor = value.substring(3);
			String alpha = value.substring(1, 3);
			return Color.valueOf(opaqueColor + alpha);
		}else{
			throw new GdxRuntimeException("Wrong type given for property " + name + ", given : " + type
				+ ", supported : string, bool, int, float, color");
		}
	}
}
