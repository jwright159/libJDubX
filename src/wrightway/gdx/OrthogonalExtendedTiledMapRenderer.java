package wrightway.gdx;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import static com.badlogic.gdx.graphics.g2d.Batch.*;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;

public class OrthogonalExtendedTiledMapRenderer extends OrthogonalTiledMapRenderer{
	public OrthogonalExtendedTiledMapRenderer(TiledMap map){
		super(map);
	}
	public OrthogonalExtendedTiledMapRenderer(TiledMap map, float unitScale){
		super(map, unitScale);
	}
	
	@Override
	public void renderTileLayer(TiledMapTileLayer layer){
		final Color batchColor = batch.getColor();
		final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

		final int layerWidth = layer.getWidth();
		final int layerHeight = layer.getHeight();

		final float layerTileWidth = layer.getTileWidth() * unitScale;
		final float layerTileHeight = layer.getTileHeight() * unitScale;

		final float layerOffsetX = layer.getRenderOffsetX() * unitScale;
		// offset in tiled is y down, so we flip it
		final float layerOffsetY = -layer.getRenderOffsetY() * unitScale;

		final int col0 = (int)((viewBounds.x - layerOffsetX) / layerTileWidth - 1);
		final int col3 = (int)((viewBounds.x + viewBounds.width + layerTileWidth - layerOffsetX) / layerTileWidth);
		final int col1 = Math.max(0, col0);
		final int col2 = Math.min(layerWidth, col3);

		final int row0 = (int)((viewBounds.y - layerOffsetY) / layerTileHeight - 1);
		final int row3 = (int)((viewBounds.y + viewBounds.height + layerTileHeight - layerOffsetY) / layerTileHeight);
		final int row1 = Math.max(0, row0);
		final int row2 = Math.min(layerHeight, row3);

		float y = row3 * layerTileHeight + layerOffsetY;
		float xStart = col0 * layerTileWidth + layerOffsetX;
		final float[] vertices = this.vertices;

		Log.debug(col0, col1, col2, col3, row0, row1, row2, row3, xStart, y);

		for(int row = row3; row >= row0; row--){
			float x = xStart;
			for(int col = col0; col < col3; col++){
				final Cell cell = layer.getCell(Math.min(Math.max(col, col1), col2-1), Math.min(Math.max(row, row1), row2-1));
				//Log.debug("Cell", col, row, Math.min(Math.max(col, col1), col2-1), Math.min(Math.max(row, row1), row2-1));
				if(cell == null){
					//Log.debug("Null");
					x += layerTileWidth;
					continue;
				}

				final TiledMapTile tile = cell.getTile();

				if(tile != null){
					final boolean flipX = cell.getFlipHorizontally();
					final boolean flipY = cell.getFlipVertically();
					final int rotations = cell.getRotation();

					TextureRegion region = tile.getTextureRegion();

					float x1 = x + tile.getOffsetX() * unitScale;
					float y1 = y + tile.getOffsetY() * unitScale;
					float x2 = x1 + region.getRegionWidth() * unitScale;
					float y2 = y1 + region.getRegionHeight() * unitScale;

					float u1 = region.getU();
					float v1 = region.getV2();
					float u2 = region.getU2();
					float v2 = region.getV();

					vertices[X1] = x1;
					vertices[Y1] = y1;
					vertices[C1] = color;
					vertices[U1] = u1;
					vertices[V1] = v1;

					vertices[X2] = x1;
					vertices[Y2] = y2;
					vertices[C2] = color;
					vertices[U2] = u1;
					vertices[V2] = v2;

					vertices[X3] = x2;
					vertices[Y3] = y2;
					vertices[C3] = color;
					vertices[U3] = u2;
					vertices[V3] = v2;

					vertices[X4] = x2;
					vertices[Y4] = y1;
					vertices[C4] = color;
					vertices[U4] = u2;
					vertices[V4] = v1;

					if(flipX){
						float temp = vertices[U1];
						vertices[U1] = vertices[U3];
						vertices[U3] = temp;
						temp = vertices[U2];
						vertices[U2] = vertices[U4];
						vertices[U4] = temp;
					}
					if(flipY){
						float temp = vertices[V1];
						vertices[V1] = vertices[V3];
						vertices[V3] = temp;
						temp = vertices[V2];
						vertices[V2] = vertices[V4];
						vertices[V4] = temp;
					}
					if(rotations != 0){
						switch(rotations){
							case Cell.ROTATE_90: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V2];
									vertices[V2] = vertices[V3];
									vertices[V3] = vertices[V4];
									vertices[V4] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U2];
									vertices[U2] = vertices[U3];
									vertices[U3] = vertices[U4];
									vertices[U4] = tempU;
									break;
								}
							case Cell.ROTATE_180: {
									float tempU = vertices[U1];
									vertices[U1] = vertices[U3];
									vertices[U3] = tempU;
									tempU = vertices[U2];
									vertices[U2] = vertices[U4];
									vertices[U4] = tempU;
									float tempV = vertices[V1];
									vertices[V1] = vertices[V3];
									vertices[V3] = tempV;
									tempV = vertices[V2];
									vertices[V2] = vertices[V4];
									vertices[V4] = tempV;
									break;
								}
							case Cell.ROTATE_270: {
									float tempV = vertices[V1];
									vertices[V1] = vertices[V4];
									vertices[V4] = vertices[V3];
									vertices[V3] = vertices[V2];
									vertices[V2] = tempV;

									float tempU = vertices[U1];
									vertices[U1] = vertices[U4];
									vertices[U4] = vertices[U3];
									vertices[U3] = vertices[U2];
									vertices[U2] = tempU;
									break;
								}
						}
					}
					batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
				}
				x += layerTileWidth;
			}
			y -= layerTileHeight;
		}
	}
}
