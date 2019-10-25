package com.github.jwright159.gdx;

import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.*;

public class FocusTable extends Table{
	private Cell<Button> cell;
	private int cellIndex = -1;
	private Array<Cell<Button>> registeredCells = new Array<Cell<Button>>();
	private boolean isFocussed;
	
	public FocusTable(){
		this(null);
	}
	public FocusTable(Skin skin){
		super(skin);
		addListener(new InputListener(){
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button){
				unfocus();
				return false;
			}
		});
	}
	
	public void focus(){
		if(nullCell())
			return;
		Log.debug("Focused", cell.getActor(), cell.getColumn(), cell.getRow());
		isFocussed = true;
		cell.getActor().getClickListener().enter(null, 0, 0, -1, null);
	}
	public void unfocus(){
		if(cell == null)
			return;
		Log.debug("Unfocused", cell.getActor(), cell.getColumn(), cell.getRow());
		isFocussed = false;
		cell.getActor().getClickListener().exit(null, 0, 0, -1, null);
		cell = null;
		cellIndex = -1;
	}
	public void refocus(int index){
		unfocus();
		cellIndex = index;
		cell = registeredCells.get(index);
		focus();
	}
	
	private boolean nullCell(){
		Log.debug("Is null?", cell == null);
		if(cell != null)
			return false;
		refocus(0);
		return true;
	}
	
	public void clickFocus(){
		if(nullCell())
			return;
		cell.getActor().toggle();
	}
	
	public boolean isFocussed(){
		return isFocussed;
	}
	
	public void registerFocus(Cell<Button> cell){
		registeredCells.add(cell);
	}
	public void unregisterFocus(Cell<Button> cell){
		registeredCells.removeValue(cell, true);
	}
	
	public void handleFocus(int keycode){
		switch(keycode){
			case Input.Keys.UP:
				focusUp();
				break;
			case Input.Keys.DOWN:
				focusDown();
				break;
			case Input.Keys.LEFT:
				focusLeft();
				break;
			case Input.Keys.RIGHT:
				focusRight();
				break;
			case Input.Keys.ENTER:
				clickFocus();
				break;
			default:
				throw new IllegalArgumentException("invalid keycode");
		}
	}
	
	public void focusUp(){
		Log.debug("Going up");
		if(nullCell())
			return;
		if(cellIndex == 0)
			return;

		int newCellIndex = cellIndex;
		Cell<Button> newCell;
		do{
			newCell = registeredCells.get(--newCellIndex);
			Log.debug("Checking", cellIndex, cell.getActorX(), cell.getRow(), "against", newCellIndex, newCell.getActorX(), newCell.getRow());
		}while(newCell.getRow() > cell.getRow()-2 && newCell.getActorX() > cell.getActorX());
		Log.debug("Done checking");
		
		if(newCell.getRow() == cell.getRow()-2)
			newCellIndex++;
		
		refocus(newCellIndex);
	}
	public void focusDown(){
		Log.debug("Going down");
		if(nullCell())
			return;
		if(cellIndex+1 == registeredCells.size)
			return;
		
		int newCellIndex = cellIndex;
		Cell<Button> newCell;
		boolean useLast;
		do{
			newCell = registeredCells.get(++newCellIndex);
			Log.debug("Checking", cellIndex, cell.getActorX(), cell.getRow(), "against", newCellIndex, newCell.getActorX(), newCell.getRow());
			useLast = newCellIndex == registeredCells.size - 1 && (newCell.getRow() == cell.getRow() || newCell.getActorX() <= cell.getActorX());
		}while(newCell.getRow() < cell.getRow()+2 && newCell.getActorX() <= cell.getActorX() && newCellIndex < registeredCells.size - 1);
		Log.debug("Done checking");
		
		// Use cell to left, except when cell is final cell
		if(!useLast || newCell.getRow() > cell.getRow() + 1)
			newCellIndex--;
		
		refocus(newCellIndex);
	}
	public void focusLeft(){
		Log.debug("Going left");
		if(nullCell())
			return;
		if(cellIndex == 0)
			return;

		int newCellIndex = cellIndex;
		Cell<Button> newCell = registeredCells.get(--newCellIndex);
		Log.debug("Checking", cellIndex, cell.getColumn(), cell.getRow(), "against", newCellIndex, newCell.getColumn(), newCell.getRow());
		Log.debug("Done checking");

		if(newCell.getRow() != cell.getRow())
			return;

		refocus(newCellIndex);
	}
	public void focusRight(){
		Log.debug("Going left");
		if(nullCell())
			return;
		if(cellIndex+1 == registeredCells.size)
			return;

		int newCellIndex = cellIndex;
		Cell<Button> newCell = registeredCells.get(++newCellIndex);
		Log.debug("Checking", cellIndex, cell.getColumn(), cell.getRow(), "against", newCellIndex, newCell.getColumn(), newCell.getRow());
		Log.debug("Done checking");

		if(newCell.getRow() != cell.getRow())
			return;

		refocus(newCellIndex);
	}

	@Override
	public void clearChildren(){
		super.clearChildren();
		registeredCells.clear();
		cell = null;
		cellIndex = -1;
	}
	@Override
	public boolean removeActor(Actor actor, boolean unfocus){
		if(!super.removeActor(actor, unfocus))
			return false;
		Cell<Button> remove = null;
		for(Cell<Button> cell : registeredCells)
			if(cell.getActor() == actor){
				remove = cell;
			}
		if(remove == null)
			return true;
		int i = registeredCells.indexOf(remove, true);
		registeredCells.removeIndex(i);
		if(i <= cellIndex)
			refocus(i-1);
		return true;
	}
}
