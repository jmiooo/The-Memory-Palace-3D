package com.example.memorymockup;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class MemoryView extends View {
	/* Constants for colors of rooms, types of rooms (Elaborated below), and rotations */
	/* 0: 3 block line, 1: 4 block line,
	 * 2: 4 block square, 3: 9 block square,
	 * 4: 4 block L, 5: 5 block L,
	 * 6: 5 block thumb */
	public static final int[] COLORS =
		{Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE, Color.GRAY};
	
	public static final int THREE_LINE = 0;
	public static final int FOUR_LINE = 1;
	public static final int FOUR_SQUARE = 2;
	public static final int NINE_SQUARE = 3;
	public static final int FOUR_EL = 4;
	public static final int FIVE_EL = 5;
	public static final int FIVE_THUMB = 6;
	public static final int[] TYPES =
		{THREE_LINE, FOUR_LINE, FOUR_SQUARE, NINE_SQUARE, FOUR_EL, FIVE_EL, FIVE_THUMB};
	
	public static final int ZERO_DEG = 0;
	public static final int NINETY_DEG = 1;
	public static final int ONE_EIGHTY_DEG = 2;
	public static final int TWO_SEVENTY_DEG = 3;
	public static final int[] ROTATIONS =
		{ZERO_DEG, NINETY_DEG, ONE_EIGHTY_DEG, TWO_SEVENTY_DEG};
	
	public static final int centerX = 550;
	public static final int centerY = 700;
	public static final int tileSize = 140;
	public static final int tileGridSize = 6;
	
	/* A bunch of room utility functions, used for both impossible and non-impossible spaces */
	public static class RoomUtility {
		public static void rotateSquares(int[][] squares, int rotation) {
			switch (rotation) {
				case ZERO_DEG:
					break;
				case NINETY_DEG:
					for (int i = 0; i < squares.length; i++) {
						int temp = squares[i][0];
						squares[i][0] = -squares[i][1];
						squares[i][1] = temp;
					}
					break;
				case ONE_EIGHTY_DEG:
					for (int i = 0; i < squares.length; i++) {
						squares[i][0] = -squares[i][0];
						squares[i][1] = -squares[i][1];
					}
					break;
				case TWO_SEVENTY_DEG:
					for (int i = 0; i < squares.length; i++) {
						int temp = squares[i][0];
						squares[i][0] = squares[i][1];
						squares[i][1] = -temp;
					}
					break;
			}
		}
		
		public static void rotateDirections(int[] directions, int rotation) {
			for (int i = 0; i < directions.length; i++) {
				directions[i] = (directions[i] + rotation) % 4;
			}
		}
		
		public static int[][] getOccupiedSquares(int x, int y, int type, int rotation) {
			int[][] squares = new int[0][0];
			
			switch (type) {
				case THREE_LINE:
					squares = new int[][] {new int[] {0, 0},
										   new int[] {0, 1},
										   new int[] {0, 2}};
					break;
				case FOUR_LINE:
					squares = new int[][] {new int[] {0, 0},
										   new int[] {0, 1},
										   new int[] {0, 2},
										   new int[] {0, 3}};
					break;
				case FOUR_SQUARE:
					squares = new int[][] {new int[] {0, 0},
										   new int[] {0, 1},
										   new int[] {1, 0},
										   new int[] {1, 1}};
					break;
				case NINE_SQUARE:
					squares = new int[][] {new int[] {0, 0},
										   new int[] {0, 1},
										   new int[] {0, 2},
										   new int[] {1, 0},
										   new int[] {1, 1},
										   new int[] {1, 2},
										   new int[] {2, 0},
										   new int[] {2, 1},
										   new int[] {2, 2}};
					break;
				case FOUR_EL:
					squares = new int[][] {new int[] {0, 0},
										   new int[] {0, 1},
										   new int[] {0, 2},
										   new int[] {1, 0}};
					break;
				case FIVE_EL:
					squares = new int[][] {new int[] {0, 0},
										   new int[] {0, 1},
										   new int[] {0, 2},
										   new int[] {1, 0},
										   new int[] {2, 0}};
					break;
				case FIVE_THUMB:
					squares = new int[][] {new int[] {0, 0},
										   new int[] {0, 1},
										   new int[] {0, 2},
										   new int[] {1, 0},
										   new int[] {1, 1}};
					break;
			}
			
			rotateSquares(squares, rotation);
			
			for (int i = 0; i < squares.length; i++) {
				squares[i][0] += x;
				squares[i][1] += y;
			}
			
			return squares;
		}
		
		/* The placement of doors are limited as of now, but can be expanded later */
		public static List<Door> getDoors(int x, int y, int type, int rotation) {
			int[][] squares = new int[0][0];
			int[] directions = new int[0];
			
			switch (type) {
				case THREE_LINE:
					squares = new int[][] {new int[] {0, -1},
										   new int[] {1, 1},
										   new int[] {0, 3},
										   new int[] {-1, 1}};
					directions = new int[] {1, 2, 3, 0};
					break;
				case FOUR_LINE:
					squares = new int[][] {new int[] {0, -1},
										   new int[] {1, 2},
										   new int[] {0, 4},
										   new int[] {-1, 1}};
					directions = new int[] {1, 2, 3, 0};
					break;
				case FOUR_SQUARE:
					squares = new int[][] {new int[] {0, -1},
										   new int[] {2, 0},
										   new int[] {1, 2},
										   new int[] {-1, 1}};
					directions = new int[] {1, 2, 3, 0};
					break;
				case NINE_SQUARE:
					squares = new int[][] {new int[] {0, -1},
										   new int[] {3, 0},
										   new int[] {2, 3},
										   new int[] {-1, 2}};
					directions = new int[] {1, 2, 3, 0};
					break;
				case FOUR_EL:
					squares = new int[][] {new int[] {0, -1},
										   new int[] {2, 0},
										   new int[] {0, 3},
										   new int[] {-1, 1}};
					directions = new int[] {1, 2, 3, 0};
					break;
				case FIVE_EL:
					squares = new int[][] {new int[] {0, -1},
										   new int[] {3, 0},
										   new int[] {0, 3},
										   new int[] {-1, 1}};
					directions = new int[] {1, 2, 3, 0};
					break;
				case FIVE_THUMB:
					squares = new int[][] {new int[] {0, -1},
										   new int[] {2, 1},
										   new int[] {0, 3},
										   new int[] {-1, 1}};
					directions = new int[] {1, 2, 3, 0};
					break;
			}
			
			rotateSquares(squares, rotation);
			rotateDirections(directions, rotation);
			
			for (int i = 0; i < squares.length; i++) {
				squares[i][0] += x;
				squares[i][1] += y;
			}
			
			List<Door> result = new ArrayList<Door>();
			
			for (int i = 0; i < squares.length; i++) {
				result.add(new Door(squares[i][0], squares[i][1], 
									directions[i], -1, -1));
			}
			
			return result;
		}
	}
	
	/* Door, Item, and Room classes, which are essential to both impossible and
	 * non-impossible spaces
	 */
	public static class Door {
		private int x, y; // Relative to base of room
		private int direction; // Direction when exiting from room
		private int destination; //Next room number
		private int destinationDoor; //Door in next room
		
		public Door(int x, int y, int direction, int destination, int destinationDoor) {
			this.x = x;
			this.y = y;
			this.direction = direction;
			this.destination = destination;
			this.destinationDoor = destination;
		}
		
		public int[] getPosition() {
			return new int[] {x, y};
		}
		
		public int getDirection() {
			return direction;
		}
		
		public int getDestination() {
			return destination;
		}
		
		public void setDestination(int destination) {
			this.destination = destination;
		}
		
		public int getDestinationDoor() {
			return destinationDoor;
		}
		
		public void setDestinationDoor(int destinationDoor) {
			this.destinationDoor = destinationDoor;
		}
	}
	
	public static class Item {
		public final int x, y; // Relative to base of room
		private String hint;
		
		public Item(int x, int y, String hint) {
			this.x = x;
			this.y = y;
			this.hint = hint;
		}
		
		public int[] getPosition() {
			return new int[] {x, y};
		}
		
		public String getHint() {
			return hint; 
		}
		
		public void updateHint(String hint) {
			this.hint = hint;
		}
	}
	
	public static class Room {
		private int number;
		private int x, y;
		private int type;
		private int rotation;
		private int color;
		private List<Door> doors;
		private List<Item> items;
		private int[][] occupiedSquares;
		
		public Room(int number, int x, int y, int type, int rotation, int color) {
			this.number = number;
			this.x = x;
			this.y = y;
			this.type = type;
			this.rotation = rotation;
			this.color = color;
			this.doors = RoomUtility.getDoors(x, y, type, rotation);
			this.items = new ArrayList<Item>();
			this.occupiedSquares = null;
		}
		
		public int getNumber() {
			return this.number;
		}
		
		public int[] getPosition() {
			return new int[] {x, y};
		}
		
		public int getColor() {
			return this.color;
		}
		
		public List<Door> getDoors() {
			return this.doors;
		}
		
		public List<Item> getItems() {
			return this.items;
		}
		
		public int[][] getOccupiedSquares() {
			if (null == this.occupiedSquares) {
				this.occupiedSquares = 
					RoomUtility.getOccupiedSquares(this.x, this.y, this.type, this.rotation);
			}
			
			return this.occupiedSquares;
		}
	}
	
	/* Key and RoomGrid are only used for non-impossible spaces */
	/*public static class Key {
	    private final int x;
	    private final int y;

	    public Key(int x, int y) {
	        this.x = x;
	        this.y = y;
	    }

	    @Override
	    public boolean equals(Object o) {
	        if (this == o)
	        	return true;
	        if (!(o instanceof Key))
	        	return false;
	        Key key = (Key) o;
	        return x == key.x && y == key.y;
	    }

	    @Override
	    public int hashCode() {
	        int result = x;
	        result = 31 * result + y;
	        return result;
	    }
	}*/
	
	// Grid-view of rooms
	/*public static class RoomGrid {
		private HashMap<Key, Integer> roomGrid;
		
		public RoomGrid() {
			this.roomGrid = new HashMap<Key, Integer>();
		}
		
		public void addSquare(int[] square, int number) {
			Key key = new Key(square[0], square[1]);
			roomGrid.put(key, number);
		}
		
		public boolean checkSquareEmpty(int[] square) {
			Key key = new Key(square[0], square[1]);
			return (null == roomGrid.get(key)) ? true : false;
		}
		
		public void addRoom(Room room) {
			int[][] occupiedSquares = room.getOccupiedSquares();
			
			for (int i = 0; i < occupiedSquares.length; i++ ) {
				addSquare(occupiedSquares[i], room.number);
			}
		}
		
		public boolean checkRoomPlaceable(Room room) {
			int[][] occupiedSquares = room.getOccupiedSquares();
			boolean result = true;
			
			for (int i = 0; i < occupiedSquares.length; i++ ) {
				result = result && checkSquareEmpty(occupiedSquares[i]);
			}
			
			return result;
		}
	}*/
	
	/* Main room manager class */
	private class RoomManager {
		protected Room currentRoom;
		protected List<Room> rooms;
		protected List<Integer> path; /* List of room numbers */
		protected int lastDoorIndex;
		
		protected Random randomGenerator;
		
		public RoomManager() {
			rooms = new ArrayList<Room>();
			path = new ArrayList<Integer>();
			
			randomGenerator = new Random();
			int type = randomGenerator.nextInt(TYPES.length);
			int rotation = 0;
			switch (type) {
				case THREE_LINE:
					rotation = randomGenerator.nextInt(2);
					break;
				case FOUR_SQUARE:
				case NINE_SQUARE:
					rotation = 0;
					break;
				case FOUR_LINE:
				case FOUR_EL:
				case FIVE_EL:
				case FIVE_THUMB:
					rotation = randomGenerator.nextInt(ROTATIONS.length);
					break;
				default:
					break;
			}
			int color = COLORS[randomGenerator.nextInt(COLORS.length)];
			currentRoom = new Room(0, 0, 0, type, rotation, color);
			
			rooms.add(currentRoom);
			path.add(0);
			
			lastDoorIndex = -1;
		}
		
		public Room getCurrentRoom() {
			return this.currentRoom;
		}
		
		public void setCurrentRoom(Room room) {
			this.currentRoom = room;
		}
		
		public List<Room> getRooms() {
			return this.rooms;
		}
		
		public void setRooms(List<Room> rooms) {
			this.rooms = rooms;
		}
		
		public List<Integer> getPath() {
			return this.path;
		}
		
		public void setPath(List<Integer> path) {
			this.path = path;
		}
		
		public int getLastDoorIndex() {
			return this.lastDoorIndex;
		}
		
		public void setLastDoorIndex(int lastDoorIndex) {
			this.lastDoorIndex = lastDoorIndex;
		}
		
		public void processMove(int i) {
		}
	}
	
	private class ImpossibleRoomManager extends RoomManager {
		public ImpossibleRoomManager() {
			super();
		}
		
		public void processMove(int doorIndex) {
			Door door = currentRoom.getDoors().get(doorIndex);
			int direction = door.getDirection();
			int destination = door.getDestination();
			
			if (destination >= 0) {
				currentRoom = rooms.get(destination);
				path.add(destination);
				lastDoorIndex = door.getDestinationDoor();
			}
			else {
				int number = rooms.size();
				int type = randomGenerator.nextInt(TYPES.length);
				int rotation = 0;
				switch (type) {
					case THREE_LINE:
						rotation = randomGenerator.nextInt(2);
						break;
					case FOUR_SQUARE:
					case NINE_SQUARE:
						rotation = 0;
						break;
					case FOUR_LINE:
					case FOUR_EL:
					case FIVE_EL:
					case FIVE_THUMB:
						rotation = randomGenerator.nextInt(ROTATIONS.length);
						break;
					default:
						break;
				}
				int color = COLORS[randomGenerator.nextInt(COLORS.length)];
				Room newRoom = new Room(number, 0, 0, type, rotation, color);				
				List<Door> newDoors = newRoom.getDoors();
				List<Integer> possibleDoorIndices = new ArrayList<Integer> ();
				for (int i = 0; i < newDoors.size(); i++) {
					if ((newDoors.get(i).getDirection() + 2) % 4 == direction) {
						possibleDoorIndices.add(i);
					}
				}
				int newDoorIndex = possibleDoorIndices.get(randomGenerator.nextInt(possibleDoorIndices.size()));
				System.out.println(newDoorIndex);

				rooms.add(newRoom);
				door.setDestination(number);
				door.setDestinationDoor(newDoorIndex);
				currentRoom = newRoom;
				
				path.add(number);
				
				lastDoorIndex = newDoorIndex;
			}
		}
		
		//public void moveRoom(int x, int y) {
			/*switch (direction) {
				case MainActivity.LEFT:				
					this.roomX -= 1;
					break;
				case MainActivity.UP:				
					this.roomY += 1;
					break;
				case MainActivity.RIGHT:				
					this.roomX += 1;
					break;
				case MainActivity.DOWN:				
					this.roomY -= 1;
					break;
			}
			
			path.add(direction);
			this.processMove();*/
		//}
	}
	
	private class NonImpossibleRoomManager extends RoomManager {
		//protected RoomGrid roomGrid;
		
		public NonImpossibleRoomManager() {
			super();
			
			//roomGrid = new RoomGrid();
			//roomGrid.addRoom(currentRoom);
		}
		
		public void processMove(int doorIndex) {
			Door door = currentRoom.getDoors().get(doorIndex);
			int direction = door.getDirection();
			int destination = door.getDestination();
			
			if (destination >= 0) {
				currentRoom = rooms.get(destination);
				path.add(destination);
				lastDoorIndex = door.getDestinationDoor();
			}
			else {
				int number = rooms.size();
				int type = randomGenerator.nextInt(TYPES.length);
				int rotation = 0;
				switch (type) {
					case THREE_LINE:
						rotation = randomGenerator.nextInt(2);
						break;
					case FOUR_SQUARE:
					case NINE_SQUARE:
						rotation = 0;
						break;
					case FOUR_LINE:
					case FOUR_EL:
					case FIVE_EL:
					case FIVE_THUMB:
						rotation = randomGenerator.nextInt(ROTATIONS.length);
						break;
					default:
						break;
				}
				int color = COLORS[randomGenerator.nextInt(COLORS.length)];
				Room newRoom = new Room(number, 0, 0, type, rotation, color);				
				List<Door> newDoors = newRoom.getDoors();
				List<Integer> possibleDoorIndices = new ArrayList<Integer> ();
				for (int i = 0; i < newDoors.size(); i++) {
					if ((newDoors.get(i).getDirection() + 2) % 4 == direction) {
						possibleDoorIndices.add(i);
					}
				}
				int newDoorIndex = possibleDoorIndices.get(randomGenerator.nextInt(possibleDoorIndices.size()));
				Door newDoor = newDoors.get(newDoorIndex);
				newDoor.setDestination(currentRoom.getNumber());
				newDoor.setDestinationDoor(doorIndex);

				rooms.add(newRoom);
				door.setDestination(number);
				door.setDestinationDoor(newDoorIndex);
				currentRoom = newRoom;
				
				path.add(number);
				
				lastDoorIndex = newDoorIndex;
			}
		}
		
		//public void moveRoom(int x, int y) {
			/*switch (direction) {
				case MainActivity.LEFT:				
					this.roomX -= 1;
					break;
				case MainActivity.UP:				
					this.roomY += 1;
					break;
				case MainActivity.RIGHT:				
					this.roomX += 1;
					break;
				case MainActivity.DOWN:				
					this.roomY -= 1;
					break;
			}
		
			path.add(direction);
			this.processMove();*/
		//}
	}
	
	public class SemiEquilateralTriangle extends Path {
		
		public SemiEquilateralTriangle() {
			super();
		}
		
		public void setVals(int x, int y, int width, int direction) {
			this.reset();
			
			switch (direction) {
				case MainActivity.LEFT:
					this.moveTo(x + width / 2, y + width / 2);
					this.lineTo(x + width / 2, y - width / 2);
					this.lineTo(x - width / 2, y);
					break;
				case MainActivity.UP:
					this.moveTo(x - width / 2, y + width / 2);
					this.lineTo(x + width / 2, y + width / 2);
					this.lineTo(x, y - width / 2);
					break;
				case MainActivity.RIGHT:
					this.moveTo(x - width / 2, y - width / 2);
					this.lineTo(x - width / 2, y + width / 2);
					this.lineTo(x + width / 2, y);
					break;
				case MainActivity.DOWN:
					this.moveTo(x + width / 2, y - width / 2);
					this.lineTo(x - width / 2, y - width / 2);
					this.lineTo(x, y + width / 2);
					break;
			}
		}
	}
	
	//public ShapeDrawable background;
	//public ShapeDrawable[] corners;
	public ShapeDrawable[] squareTiles;
	public Paint doorPaint;
	public Paint doorPaintLast;
	public SemiEquilateralTriangle[] doorTiles;
	public ShapeDrawable player;
	public Paint textPaint;
	public Camera camera;
	public Matrix matrix, inverse;
	public int adjustX, adjustY;
	
	public int mode;
	public int task;
	
	public static int IMPOSSIBLE = 0;
	public static int NONIMPOSSIBLE = 1;
	
	public static int MATCH = 0;
	public static int ENTRY = 1;
	
	public RoomManager roomManager;
	
	public Gson gson;
	
	// Just for matching
	public List<Integer> pathToFollow;
	public String status = "";
	
	public MemoryView(Context context) {
		super(context);
        setFocusable(true);
        initMemoryView(context);
	}
	
	public MemoryView(Context context, AttributeSet attrs) {
		super(context, attrs);
        setFocusable(true);
        initMemoryView(context);
	}
	
	public void initMemoryView(Context context) {		
		squareTiles = new ShapeDrawable[tileGridSize * tileGridSize];
		for (int i = 0; i < squareTiles.length; i++) {
			squareTiles[i] = new ShapeDrawable(new RectShape());
		}
		
		doorPaint = new Paint(); 
		doorPaint.setStyle(Style.FILL);
	    doorPaint.setColor(0xFFA52A2A);
	    
	    doorPaintLast = new Paint(); 
		doorPaintLast.setStyle(Style.FILL);
	    doorPaintLast.setColor(0xFF000000);
		
	    doorTiles = new SemiEquilateralTriangle[tileGridSize * tileGridSize];
		for (int i = 0; i < doorTiles.length; i++) {
			doorTiles[i] = new SemiEquilateralTriangle();
		}
		
		player = new ShapeDrawable(new OvalShape());
		player.getPaint().setColor(0xFF000000);
		
		textPaint = new Paint(); 
	    textPaint.setColor(Color.BLACK); 
	    textPaint.setTextSize(100);
	    
	    camera = new Camera();
	    matrix = new Matrix();
	    inverse = new Matrix();
	    
	    mode = IMPOSSIBLE;
	    
	    gson = new Gson();
	}
	
	public void setMode(String modeString) {
		if (modeString.equals(StartActivity.IMPOSSIBLE)) {
			mode = IMPOSSIBLE;
			roomManager = new ImpossibleRoomManager();
		}
		else if (modeString.equals(StartActivity.NONIMPOSSIBLE)) {
			mode = NONIMPOSSIBLE;
			roomManager = new NonImpossibleRoomManager();
		}
	}
	
	public void setTask(String taskString) {
		if (taskString.equals(SetupActivity.MATCH)) {
			task = MATCH;
		}
		else if (taskString.equals(SetupActivity.ENTRY)) {
			task = ENTRY;
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		Room currentRoom = roomManager.getCurrentRoom();
		int[] position = currentRoom.getPosition();
		int x = position[0];
		int y = position[1];
		int color = currentRoom.getColor();
		int[][] squares = currentRoom.getOccupiedSquares();
		List<Door> doors = currentRoom.getDoors();
		int squareTileIndex = 0;
		int doorTileIndex = 0;
		int lastDoorIndex = roomManager.getLastDoorIndex();
		
		
		int minX, minY, maxX, maxY;
		minX = minY = maxX = maxY = 0;
		for (int i = 0; i < squares.length; i++) {
	    	int squareX = squares[i][0] - x;
	    	int squareY = squares[i][1] - y;
	    	
	    	minX = Math.min(minX, squareX);
	    	minY = Math.min(minY, squareY);
	    	maxX = Math.max(maxX, squareX);
	    	maxY = Math.max(maxY, squareY);
		}
		for (int i = 0; i < doors.size(); i++) {
	    	int[] doorPosition = doors.get(i).getPosition();
	    	int doorX = doorPosition[0] - x;
	    	int doorY = doorPosition[1] - y;
	    	
	    	minX = Math.min(minX, doorX);
	    	minY = Math.min(minY, doorY);
	    	maxX = Math.max(maxX, doorX);
	    	maxY = Math.max(maxY, doorY);
	    }
		adjustX = -(minX + maxX) * tileSize / 2;
		adjustY = -(minY + maxY) * tileSize / 2;

		camera.save();
		camera.rotate((float) 35.264, 0, 45);
		camera.getMatrix(matrix);
		matrix.preTranslate(-centerX, -centerY);
		matrix.postTranslate(centerX, centerY);
		camera.restore();
		matrix.invert(inverse);
		canvas.concat(matrix);
	    
	    for (int i = 0; i < squares.length; i++) {
	    	int squareX = squares[i][0] - x;
	    	int squareY = squares[i][1] - y;
	    	
			squareTiles[squareTileIndex].setBounds(centerX + adjustX + squareX * tileSize - tileSize * 9 / 20,
												   centerY + adjustY + squareY * tileSize - tileSize * 9 / 20,
												   centerX + adjustX + squareX * tileSize + tileSize * 9 / 20,
												   centerY + adjustY + squareY * tileSize + tileSize * 9 / 20);
			squareTiles[squareTileIndex].getPaint().setColor(color);
			squareTiles[squareTileIndex].draw(canvas);
			squareTileIndex++;
	    }
	    
	    for (int i = 0; i < doors.size(); i++) {
	    	Door door = doors.get(i);
	    	
	    	int[] doorPosition = door.getPosition();
	    	int doorDirection = door.getDirection();
	    	int doorX = doorPosition[0] - x;
	    	int doorY = doorPosition[1] - y;
	    	
	    	doorTiles[doorTileIndex].setVals(centerX + adjustX + doorX * tileSize,
									   		 centerY + adjustY + doorY * tileSize,
											 tileSize * 27 / 40,
											 doorDirection);
	    	if (i == lastDoorIndex) {
	    		canvas.drawPath(doorTiles[doorTileIndex], doorPaintLast);
	    	}
	    	else {
	    		canvas.drawPath(doorTiles[doorTileIndex], doorPaint);
	    	}
			doorTileIndex++;
	    }
	    
	    if (lastDoorIndex >= 0) {
	    	Door door = doors.get(lastDoorIndex);
	    	int[] lastDoorPosition = door.getPosition();
	    	int lastDoorDirection = door.getDirection();
	    	int playerX = 0;
	    	int playerY = 0;
	    	
	    	switch (lastDoorDirection) {
	    		case MainActivity.LEFT:
	    			playerX = lastDoorPosition[0] - x + 1;
	    			playerY = lastDoorPosition[1] - y;
	    			break;
	    		case MainActivity.UP:
	    			playerX = lastDoorPosition[0] - x;
	    			playerY = lastDoorPosition[1] - y + 1;
	    			break;
	    		case MainActivity.RIGHT:
	    			playerX = lastDoorPosition[0] - x - 1;
	    			playerY = lastDoorPosition[1] - y;
	    			break;
	    		case MainActivity.DOWN:
	    			playerX = lastDoorPosition[0] - x;
	    			playerY = lastDoorPosition[1] - y - 1;
	    			break;
	    	}
	    	
		    player.setBounds(centerX + adjustX + playerX * tileSize - tileSize * 2 / 5,
		    				 centerY + adjustY + playerY * tileSize - tileSize * 2 / 5,
		    				 centerX + adjustX + playerX * tileSize + tileSize * 2 / 5,
		    				 centerY + adjustY + playerY * tileSize + tileSize * 2 / 5);
		    player.draw(canvas);
	    }
	    else {
	    	player.setBounds(centerX + adjustX - tileSize * 2 / 5,
			   				 centerY + adjustY - tileSize * 2 / 5,
			   				 centerX + adjustX + tileSize * 2 / 5,
			   				 centerY + adjustY + tileSize * 2 / 5);
	    	player.draw(canvas);
	    }
	    
	    canvas.restore();
	    
	    if (task == MATCH) {
	    	canvas.drawText(status, 150, 200, textPaint);
	    	status = "";
	    }
	    if (task == ENTRY) {
	    	canvas.drawText("Room Number: " + Integer.toString(currentRoom.number), 150, 200, textPaint);
	    }
	}
	
	public void moveRoom(int x, int y) {
		float[] position = new float[] {x, y};
		inverse.mapPoints(position);
		int tileX = Math.round(position[0]) - centerX - adjustX + tileSize / 2;
		int tileY = Math.round(position[1]) - centerY - adjustY + tileSize / 2;
		
		List<Door> doors = roomManager.getCurrentRoom().getDoors();
		for (int i = 0; i < doors.size(); i++) {
			Door door = doors.get(i);
			int[] doorPosition = door.getPosition();
			
			if ((doorPosition[0] - 0.5) * tileSize <= tileX && tileX < (doorPosition[0] + 1.5) * tileSize && 
				(doorPosition[1] - 0.5) * tileSize <= tileY && tileY < (doorPosition[1] + 1.5) * tileSize) {
				roomManager.processMove(i);
			}
		}
		
		invalidate();
	}
	
	public void setRoomManager(String roomInfo) {
		if (this.mode == IMPOSSIBLE) {
			this.roomManager = gson.fromJson(roomInfo, ImpossibleRoomManager.class);
		}
		else {
			this.roomManager = gson.fromJson(roomInfo, NonImpossibleRoomManager.class);
		}
		
		Room room = this.roomManager.getRooms().get(0);
		pathToFollow = this.roomManager.getPath();
		this.roomManager.setCurrentRoom(room);
		List<Integer> newPath = new ArrayList<Integer>();
		newPath.add(0);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDoorIndex(-1);
	}
	
	public boolean matchPath() {
		List<Integer> pathUntilNow = roomManager.getPath();
		
		if (pathUntilNow.size() == pathToFollow.size()) {
			for (int i = 0; i < pathUntilNow.size(); i++) {
				if (pathUntilNow.get(i) != pathToFollow.get(i)) {
					status = "Path is incorrect.";
					invalidate();
					return false;
				}
			}
			
			status = "Path is correct.";
			invalidate();
			return true;
		}
		
		status = "Path is incorrect.";
		invalidate();
		return false;
	}
	
	public String getRoomInfoStringified() {
		return gson.toJson(roomManager);
	}
	
	public void resetPath() {
		Room room = this.roomManager.getRooms().get(0);
		this.roomManager.setCurrentRoom(room);
		List<Integer> newPath = new ArrayList<Integer>();
		newPath.add(0);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDoorIndex(-1);
		invalidate();
	}
}