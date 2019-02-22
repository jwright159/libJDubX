package wrightway.gdx;

import com.badlogic.gdx.files.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.g2d.freetype.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.*;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

public class WSkin extends Skin{
	public WSkin(){
		super();
	}
	public WSkin(FileHandle json, TextureAtlas atlas){
		super(json, atlas);
	}
	/** Overridden to do NineRegions */
	@Override
	public NinePatch getPatch(String name){
		NinePatch patch = optional(name, NinePatch.class);
		if(patch != null) return patch;

		try{
			TextureRegion region = getRegion(name);
			if(region instanceof TextureAtlas.AtlasRegion){
				int[] splits = ((TextureAtlas.AtlasRegion)region).splits;
				if(splits != null){
					patch = regionTiles(region) ? new NinePatch(region, splits[0], splits[1], splits[2], splits[3]) : new NinePatch(region, splits[0], splits[1], splits[2], splits[3]);
					int[] pads = ((TextureAtlas.AtlasRegion)region).pads;
					if(pads != null) patch.setPadding(pads[0], pads[1], pads[2], pads[3]);
				}
			}
			if(patch == null) patch = regionTiles(region) ? new NineRegion(region) : new NinePatch(region);
			add(name, patch, NinePatch.class);
			return patch;
		}catch(GdxRuntimeException ex){
			throw new GdxRuntimeException("No NinePatch, TextureRegion, or Texture registered with name: " + name);
		}
	}
	@Override
	public Drawable newDrawable(Drawable drawable, Color tint){
		Drawable newDrawable;
		if(drawable instanceof TextureRegionDrawable)
			newDrawable = ((TextureRegionDrawable)drawable).tint(tint);
		else if(drawable instanceof NinePatchDrawable)
			newDrawable = ((NinePatchDrawable)drawable).getPatch() instanceof NineRegion ? NineRegion.tint((NinePatchDrawable)drawable, tint) : ((NinePatchDrawable)drawable).tint(tint);
		else if(drawable instanceof SpriteDrawable)
			newDrawable = ((SpriteDrawable)drawable).tint(tint);
		else
			throw new GdxRuntimeException("Unable to copy, unknown drawable type: " + drawable.getClass());

		if(newDrawable instanceof BaseDrawable){
			BaseDrawable named = (BaseDrawable)newDrawable;
			if(drawable instanceof BaseDrawable)
				named.setName(((BaseDrawable)drawable).getName() + " (" + tint + ")");
			else
				named.setName(" (" + tint + ")");
		}

		return newDrawable;
	}
	/** Overriden to do FreeTypeFonts
	 * By raeleus at https://github.com/raeleus/Pen-to-the-Paper-UI, released under the MIT license
	*/
	@Override
	protected Json getJsonLoader(final FileHandle skinFile){
		Json json = super.getJsonLoader(skinFile);
		final Skin skin = this;

		json.setSerializer(FreeTypeFontGenerator.class, new Json.ReadOnlySerializer<FreeTypeFontGenerator>() {
				@Override
				public FreeTypeFontGenerator read(Json json, JsonValue jsonData, Class type){
					String path = json.readValue("font", String.class, jsonData);
					jsonData.remove("font");

					Hinting hinting = Hinting.valueOf(json.readValue("hinting", String.class, "AutoMedium", jsonData));
					jsonData.remove("hinting");

					TextureFilter minFilter = TextureFilter.valueOf(json.readValue("minFilter", String.class, "Nearest", jsonData));
					jsonData.remove("minFilter");

					TextureFilter magFilter = TextureFilter.valueOf(json.readValue("magFilter", String.class, "Nearest", jsonData));
					jsonData.remove("magFilter");

					FreeTypeFontParameter parameter = json.readValue(FreeTypeFontParameter.class, jsonData);
					parameter.hinting = hinting;
					parameter.minFilter = minFilter;
					parameter.magFilter = magFilter;
					FreeTypeFontGenerator generator = new FreeTypeFontGenerator(skinFile.parent().child(path)){
						@Override
						public BitmapFont generateFont(FreeTypeFontParameter parameter, FreeTypeBitmapFontData data){
							BitmapFont font = super.generateFont(parameter, data);
							font = new BitmapFont(font.getData(), font.getRegions(), true){
								@Override
								public void dispose(){
									Array<TextureRegion> regions = getRegions();
									if(ownsTexture()){
										for(int i = 0; i < regions.size; i++){
											try{
												regions.get(i).getTexture().dispose();
											}catch(GdxRuntimeException e){
												Log.error(e.getMessage());
											}
										}
									}
								}
							};
							return font;
						}
					};
					BitmapFont font = generator.generateFont(parameter);
					skin.add(jsonData.name, font, BitmapFont.class);
					if(parameter.incremental){
						generator.dispose();
						return null;
					}else{
						return generator;
					}
				}
			});

		return json;
	}
	
	public static boolean regionTiles(TextureRegion region){
		return region instanceof NineRegionTextureAtlas.NineRegionAtlasRegion && ((NineRegionTextureAtlas.NineRegionAtlasRegion)region).tile;
	}
}
