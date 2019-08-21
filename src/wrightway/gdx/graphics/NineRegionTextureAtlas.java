package wrightway.gdx.graphics;

import com.badlogic.gdx.*;
import com.badlogic.gdx.files.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.Pixmap.*;
import com.badlogic.gdx.graphics.Texture.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.*;
import com.badlogic.gdx.utils.*;
import java.io.*;
import java.util.*;

import static com.badlogic.gdx.graphics.Texture.TextureWrap.*;

public class NineRegionTextureAtlas extends TextureAtlas{
	static final String[] tuple = new String[4];

	private final ObjectSet<Texture> textures = new ObjectSet<Texture>(4);
	private final Array<NineRegionAtlasRegion> regions = new Array<NineRegionAtlasRegion>();

	public static class NineRegionTextureAtlasData{
		public static class NRRegion extends TextureAtlasData.Region{
			public boolean tile;
		}

		final Array<Page> pages = new Array<Page>();
		final Array<NRRegion> regions = new Array<NRRegion>();

		public NineRegionTextureAtlasData(FileHandle packFile, FileHandle imagesDir, boolean flip){
			BufferedReader reader = new BufferedReader(new InputStreamReader(packFile.read()), 64);
			try{
				Page pageImage = null;
				while(true){
					String line = reader.readLine();
					if(line == null) break;
					if(line.trim().length() == 0)
						pageImage = null;
					else if(pageImage == null){
						FileHandle file = imagesDir.child(line);

						float width = 0, height = 0;
						if(readTuple(reader) == 2){ // size is only optional for an atlas packed with an old TexturePacker.
							width = Integer.parseInt(tuple[0]);
							height = Integer.parseInt(tuple[1]);
							readTuple(reader);
						}
						Format format = Format.valueOf(tuple[0]);

						readTuple(reader);
						TextureFilter min = TextureFilter.valueOf(tuple[0]);
						TextureFilter max = TextureFilter.valueOf(tuple[1]);

						String direction = readValue(reader);
						TextureWrap repeatX = ClampToEdge;
						TextureWrap repeatY = ClampToEdge;
						if(direction.equals("x"))
							repeatX = Repeat;
						else if(direction.equals("y"))
							repeatY = Repeat;
						else if(direction.equals("xy")){
							repeatX = Repeat;
							repeatY = Repeat;
						}

						pageImage = new Page(file, width, height, min.isMipMap(), format, min, max, repeatX, repeatY);
						pages.add(pageImage);
					}else{
						boolean rotate = Boolean.valueOf(readValue(reader));

						readTuple(reader);
						int left = Integer.parseInt(tuple[0]);
						int top = Integer.parseInt(tuple[1]);

						readTuple(reader);
						int width = Integer.parseInt(tuple[0]);
						int height = Integer.parseInt(tuple[1]);

						NRRegion region = new NRRegion();
						region.page = pageImage;
						region.left = left;
						region.top = top;
						region.width = width;
						region.height = height;
						region.name = line;
						region.rotate = rotate;

						if(readTuple(reader) == 4){ // split is optional
							region.splits = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
								Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};

							int t = readTuple(reader);
							if(t == 4){ // pad is optional, but only present with splits
								region.pads = new int[] {Integer.parseInt(tuple[0]), Integer.parseInt(tuple[1]),
									Integer.parseInt(tuple[2]), Integer.parseInt(tuple[3])};
								
								t = readTuple(reader);
							}
							
							if(t == 1){
								region.tile = Boolean.parseBoolean(tuple[0]);
								readTuple(reader);
							}
						}

						region.originalWidth = Integer.parseInt(tuple[0]);
						region.originalHeight = Integer.parseInt(tuple[1]);

						readTuple(reader);
						region.offsetX = Integer.parseInt(tuple[0]);
						region.offsetY = Integer.parseInt(tuple[1]);

						region.index = Integer.parseInt(readValue(reader));

						if(flip) region.flip = true;

						regions.add(region);
					}
				}
			}
			catch(Exception ex){
				throw new GdxRuntimeException("Error reading pack file: " + packFile, ex);
			}
			finally{
				StreamUtils.closeQuietly(reader);
			}

			regions.sort(indexComparator);
		}

		public Array<Page> getPages(){
			return pages;
		}

		public Array<NRRegion> getRegions(){
			return regions;
		}
	}

	/** Creates an empty atlas to which regions can be added. */
	public NineRegionTextureAtlas(){
	}

	/** Loads the specified pack file using {@link FileType#Internal}, using the parent directory of the pack file to find the page
	 * images. */
	public NineRegionTextureAtlas(String internalPackFile){
		this(Gdx.files.internal(internalPackFile));
	}

	/** Loads the specified pack file, using the parent directory of the pack file to find the page images. */
	public NineRegionTextureAtlas(FileHandle packFile){
		this(packFile, packFile.parent());
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner.
	 * @see #TextureAtlas(FileHandle) */
	public NineRegionTextureAtlas(FileHandle packFile, boolean flip){
		this(packFile, packFile.parent(), flip);
	}

	public NineRegionTextureAtlas(FileHandle packFile, FileHandle imagesDir){
		this(packFile, imagesDir, false);
	}

	/** @param flip If true, all regions loaded will be flipped for use with a perspective where 0,0 is the upper left corner. */
	public NineRegionTextureAtlas(FileHandle packFile, FileHandle imagesDir, boolean flip){
		this(new NineRegionTextureAtlasData(packFile, imagesDir, flip));
	}

	/** @param data May be null. */
	public NineRegionTextureAtlas(NineRegionTextureAtlasData data){
		if(data != null) load(data);
	}

	private void load(NineRegionTextureAtlasData data){
		ObjectMap<Page, Texture> pageToTexture = new ObjectMap<Page, Texture>();
		for(Page page : data.pages){
			Texture texture = null;
			if(page.texture == null){
				texture = new Texture(page.textureFile, page.format, page.useMipMaps);
				texture.setFilter(page.minFilter, page.magFilter);
				texture.setWrap(page.uWrap, page.vWrap);
			}else{
				texture = page.texture;
				texture.setFilter(page.minFilter, page.magFilter);
				texture.setWrap(page.uWrap, page.vWrap);
			}
			textures.add(texture);
			pageToTexture.put(page, texture);
		}

		for(NineRegionTextureAtlasData.NRRegion region : data.regions){
			int width = region.width;
			int height = region.height;
			NineRegionAtlasRegion atlasRegion = new NineRegionAtlasRegion(pageToTexture.get(region.page), region.left, region.top,
													  region.rotate ? height : width, region.rotate ? width : height);
			atlasRegion.index = region.index;
			atlasRegion.name = region.name;
			atlasRegion.offsetX = region.offsetX;
			atlasRegion.offsetY = region.offsetY;
			atlasRegion.originalHeight = region.originalHeight;
			atlasRegion.originalWidth = region.originalWidth;
			atlasRegion.rotate = region.rotate;
			atlasRegion.splits = region.splits;
			atlasRegion.pads = region.pads;
			atlasRegion.tile = region.tile;
			if(region.flip) atlasRegion.flip(false, true);
			regions.add(atlasRegion);
		}
	}

	/** Adds a region to the atlas. The specified texture will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion(String name, Texture texture, int x, int y, int width, int height){
		textures.add(texture);
		NineRegionAtlasRegion region = new NineRegionAtlasRegion(texture, x, y, width, height);
		region.name = name;
		region.index = -1;
		regions.add(region);
		return region;
	}

	/** Adds a region to the atlas. The texture for the specified region will be disposed when the atlas is disposed. */
	public AtlasRegion addRegion(String name, TextureRegion textureRegion){
		textures.add(textureRegion.getTexture());
		NineRegionAtlasRegion region = new NineRegionAtlasRegion(textureRegion);
		region.name = name;
		region.index = -1;
		regions.add(region);
		return region;
	}

	/** Returns all regions in the atlas. */
	public Array<? extends AtlasRegion> getRegions(){
		return regions;
	}

	/** Returns the first region found with the specified name. This method uses string comparison to find the region, so the result
	 * should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public AtlasRegion findRegion(String name){
		for(int i = 0, n = regions.size; i < n; i++)
			if(regions.get(i).name.equals(name)) return regions.get(i);
		return null;
	}

	/** Returns the first region found with the specified name and index. This method uses string comparison to find the region, so
	 * the result should be cached rather than calling this method multiple times.
	 * @return The region, or null. */
	public AtlasRegion findRegion(String name, int index){
		for(int i = 0, n = regions.size; i < n; i++){
			AtlasRegion region = regions.get(i);
			if(!region.name.equals(name)) continue;
			if(region.index != index) continue;
			return region;
		}
		return null;
	}

	/** Returns all regions with the specified name, ordered by smallest to largest {@link AtlasRegion#index index}. This method
	 * uses string comparison to find the regions, so the result should be cached rather than calling this method multiple times. */
	public Array<? extends AtlasRegion> findRegions(String name){
		Array<NineRegionAtlasRegion> matched = new Array<NineRegionAtlasRegion>(NineRegionAtlasRegion.class);
		for(int i = 0, n = regions.size; i < n; i++){
			AtlasRegion region = regions.get(i);
			if(region.name.equals(name)) matched.add(new NineRegionAtlasRegion(region));
		}
		return matched;
	}

	/** Returns the first region found with the specified name as a sprite. If whitespace was stripped from the region when it was
	 * packed, the sprite is automatically positioned as if whitespace had not been stripped. This method uses string comparison to
	 * find the region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null. */
	public Sprite createSprite(String name){
		for(int i = 0, n = regions.size; i < n; i++)
			if(regions.get(i).name.equals(name)) return newSprite(regions.get(i));
		return null;
	}

	/** Returns the first region found with the specified name and index as a sprite. This method uses string comparison to find the
	 * region and constructs a new sprite, so the result should be cached rather than calling this method multiple times.
	 * @return The sprite, or null.
	 * @see #createSprite(String) */
	public Sprite createSprite(String name, int index){
		for(int i = 0, n = regions.size; i < n; i++){
			AtlasRegion region = regions.get(i);
			if(!region.name.equals(name)) continue;
			if(region.index != index) continue;
			return newSprite(regions.get(i));
		}
		return null;
	}

	/** Returns all regions with the specified name as sprites, ordered by smallest to largest {@link AtlasRegion#index index}. This
	 * method uses string comparison to find the regions and constructs new sprites, so the result should be cached rather than
	 * calling this method multiple times.
	 * @see #createSprite(String) */
	public Array<Sprite> createSprites(String name){
		Array<Sprite> matched = new Array<Sprite>(Sprite.class);
		for(int i = 0, n = regions.size; i < n; i++){
			AtlasRegion region = regions.get(i);
			if(region.name.equals(name)) matched.add(newSprite(region));
		}
		return matched;
	}

	private Sprite newSprite(AtlasRegion region){
		if(region.packedWidth == region.originalWidth && region.packedHeight == region.originalHeight){
			if(region.rotate){
				Sprite sprite = new Sprite(region);
				sprite.setBounds(0, 0, region.getRegionHeight(), region.getRegionWidth());
				sprite.rotate90(true);
				return sprite;
			}
			return new Sprite(region);
		}
		return new AtlasSprite(region);
	}

	/** Returns the first region found with the specified name as a {@link NinePatch}. The region must have been packed with
	 * ninepatch splits. This method uses string comparison to find the region and constructs a new ninepatch, so the result should
	 * be cached rather than calling this method multiple times.
	 * @return The ninepatch, or null. */
	public NinePatch createPatch(String name){
		for(int i = 0, n = regions.size; i < n; i++){
			NineRegionAtlasRegion region = regions.get(i);
			if(region.name.equals(name)){
				int[] splits = region.splits;
				if(splits == null) throw new IllegalArgumentException("Region does not have ninepatch splits: " + name);
				NinePatch patch = region.tile ? new NineRegion(region, splits[0], splits[1], splits[2], splits[3]) : new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
				if(region.pads != null) patch.setPadding(region.pads[0], region.pads[1], region.pads[2], region.pads[3]);
				return patch;
			}
		}
		return null;
	}

	/** @return the textures of the pages, unordered */
	public ObjectSet<Texture> getTextures(){
		return textures;
	}

	/** Releases all resources associated with this TextureAtlas instance. This releases all the textures backing all TextureRegions
	 * and Sprites, which should no longer be used after calling dispose. */
	public void dispose(){
		for(Texture texture : textures)
			texture.dispose();
		textures.clear();
	}

	static final Comparator<Region> indexComparator = new Comparator<Region>() {
		public int compare(Region region1, Region region2){
			int i1 = region1.index;
			if(i1 == -1) i1 = Integer.MAX_VALUE;
			int i2 = region2.index;
			if(i2 == -1) i2 = Integer.MAX_VALUE;
			return i1 - i2;
		}
	};

	static String readValue(BufferedReader reader) throws IOException{
		String line = reader.readLine();
		int colon = line.indexOf(':');
		if(colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
		return line.substring(colon + 1).trim();
	}

	/** Returns the number of tuple values read (1, 2 or 4). */
	static int readTuple(BufferedReader reader) throws IOException{
		String line = reader.readLine();
		int colon = line.indexOf(':');
		if(colon == -1) throw new GdxRuntimeException("Invalid line: " + line);
		int i = 0, lastMatch = colon + 1;
		for(i = 0; i < 3; i++){
			int comma = line.indexOf(',', lastMatch);
			if(comma == -1) break;
			tuple[i] = line.substring(lastMatch, comma).trim();
			lastMatch = comma + 1;
		}
		tuple[i] = line.substring(lastMatch).trim();
		return i + 1;
	}

	static public class NineRegionAtlasRegion extends AtlasRegion{
		public boolean tile;

		public NineRegionAtlasRegion(Texture texture, int x, int y, int width, int height){
			super(texture, x, y, width, height);
		}

		public NineRegionAtlasRegion(NineRegionAtlasRegion region){
			super(region);
			tile = region.tile;
		}

		public NineRegionAtlasRegion(TextureRegion region){
			super(region);
		}
	}
}
