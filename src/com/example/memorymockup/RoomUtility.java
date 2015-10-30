package com.example.memorymockup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.util.Log;

public class RoomUtility {
	/* Constants for colors of rooms, types of rooms */
	public static final int[] COLORS =
		{0, 1};
	
	public static final int[] TYPES =
		{0, 1};
	
	public static final int BLOCK_SIZE = 5; /* Side length of pre-computed square of rooms */
	public static final int BLOCKS_PER_SIDE = 5; /* Number of blocks of rooms to fit along a side of the grid of rooms */
	public static final int GRID_SIZE = BLOCK_SIZE * BLOCKS_PER_SIDE; /* Side length of square grid of rooms */
	
	
	//TODO: Move these into text files
	public static final int[][] BLOCK_A = {
		{ 0, 1, 1, 1, 2 },
		{ 0, 0, 3, 3, 2 },
		{ 4, 4, 4, 3, 2 },
		{ 4, 5, 6, 6, 2 },
		{ 7, 7, 6, 8, 8 }
	};
	public static final int BLOCK_A_ROOM_COUNT = 9;
	
	public static final int[][] BLOCK_B = {
		{ 0, 1, 1, 1, 1 },
		{ 0, 2, 3, 4, 4 },
		{ 0, 2, 3, 5, 4 },
		{ 6, 2, 3, 5, 5 },
		{ 6, 6, 6, 7, 8 }
	};
	public static final int BLOCK_B_ROOM_COUNT = 9;
	
	public static final int[][][] BLOCK_CHOICES = {
		BLOCK_A,
		BLOCK_B
	};
	public static final int[] BLOCK_CHOICES_ROOM_COUNTS = {
		BLOCK_A_ROOM_COUNT,
		BLOCK_B_ROOM_COUNT
	};
	
	//public static final int MIN_DOOR_COUNT = 
	// For now, this probability kicks in when a room already has a door to its neighbor
	// TODO: Consider all possible doors to a neighbor equally as the required door
	//public static final int DOOR_PROB = 0.5
	
	
	//TODO: To make it continuously generative, the grid should be a two-way hash table.
	public static int[][] generateGrid() {
		int[][] grid = new int[GRID_SIZE][GRID_SIZE];
		
		Random randomGenerator = new Random();
		int roomsSeen = 0; // Rooms seen up to previous block; Correct after adding each block fully
		
		for (int r = 0; r < BLOCKS_PER_SIDE; r++) {
			for (int c = 0; c < BLOCKS_PER_SIDE; c++) {
				int blockIndex = randomGenerator.nextInt(BLOCK_CHOICES.length);
				int[][] block = BLOCK_CHOICES[blockIndex];
				
				for (int i = 0; i < BLOCK_SIZE; i++) {
					for (int j = 0; j < BLOCK_SIZE; j++) {
						grid[r * BLOCK_SIZE + i][c * BLOCK_SIZE + j] = roomsSeen + block[i][j];
					}
				}
				
				roomsSeen += BLOCK_CHOICES_ROOM_COUNTS[blockIndex];
			}
		}
		
		/*// Just in case we want to make the grid keep expanding later on
		List<List<Integer>> grid_list = new ArrayList<List<Integer>>();
		
		for (int i = 0; i < GRID_SIZE; i++) {
			List<Integer> row = new ArrayList<Integer>();
			
			for (int j = 0; j < GRID_SIZE) {
				
			}
		}*/
		
		return grid;
	}
	
	// Takes in a pre-generated grid that has the room layouts, and actually creates the Room objects
	// Door placement takes place here
	// TODO: Floor / Wall / Ceiling type decisions should also take place here
	// TODO: Counting room numbers again here is a bit redundant
	public static List<Room> generateRooms (int[][] grid) {
		int roomCount = 0;
		
		// Setting up the room array with the correct amount of rooms
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				int roomNumber = grid[i][j];
				roomCount = roomNumber + 1 > roomCount ? roomNumber + 1 : roomCount;
			}
		}
		
		Room[] rooms = new Room[roomCount];
		Random random = new Random();
		
		for (int i = 0; i < rooms.length; i++) {
			List<int[]> tiles = new ArrayList<int[]> ();
			List<Door> doors = new ArrayList<Door> ();
			List<Item> items = new ArrayList<Item> ();
			rooms[i] = new Room(i, tiles, doors, items, random.nextInt(TYPES.length), 0, 0);
		}
		
		// Inserting the tiles into the room
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {
				//Log.e("Inserting", Integer.toString(i) + " " + Integer.toString(j) + " " + Integer.toString(grid[i][j]));
				int roomNumber = grid[i][j];
				int[] tile = new int[] { i, j };
				rooms[roomNumber].tiles.add(tile);
			}
		}
		
		// Inserting the doors into the rooms
		for (int i = 0; i < GRID_SIZE; i++) {
			for (int j = 0; j < GRID_SIZE; j++) {

				// For each tile, we try to add a door going to the right, and going down
				// Door goes right
				if (j < GRID_SIZE - 1 && grid[i][j] != grid[i][j + 1]) {
					int sourceRoomNumber = grid[i][j];
					Room sourceRoom = rooms[sourceRoomNumber];
					int[] sourceTile = new int[] { i, j };
					Door sourceDoor = new Door(sourceTile, MainActivity.RIGHT, grid[i][j + 1]);
					sourceRoom.doors.add(sourceDoor);
					
					int destRoomNumber = grid[i][j + 1];
					Room destRoom = rooms[destRoomNumber];
					int[] destTile = new int[] { i, j + 1 };
					Door destDoor = new Door(destTile, MainActivity.LEFT, grid[i][j]);
					destRoom.doors.add(destDoor);
				}
				
				// Door goes down
				if (i < GRID_SIZE - 1 && grid[i][j] != grid[i + 1][j]) {
					int sourceRoomNumber = grid[i][j];
					Room sourceRoom = rooms[sourceRoomNumber];
					int[] sourceTile = new int[] { i, j };
					Door sourceDoor = new Door(sourceTile, MainActivity.DOWN, grid[i + 1][j]);
					sourceRoom.doors.add(sourceDoor);
					
					int destRoomNumber = grid[i + 1][j];
					Room destRoom = rooms[destRoomNumber];
					int[] destTile = new int[] { i + 1, j };
					Door destDoor = new Door(destTile, MainActivity.UP, grid[i][j]);
					destRoom.doors.add(destDoor);
				}
			}
		}
		
		return new ArrayList<Room>(Arrays.asList(rooms));
	}
	
	
	/* The placement of doors are limited as of now, but can be expanded later */
	/*public static List<Door> getDoors(int x, int y) {
		List<Door> result = new ArrayList<Door>();
		
		for (int i = 0; i < 4; i++) {
			result.add(new Door(0, 0, i, -1, -1));
		}
		
		return result;
	}*/
	
	/* Door, Item, and Room classes, which are essential to both impossible and
	 * non-impossible spaces
	 */
	public static class Door {
		private int[] pos; // Coordinate of tile
		private int direction; // Direction when exiting from room
		private int destination; //Next room number
		//private int destinationDoor; //Door in next room
		
		public Door(int[] pos, int direction, int destination) {
			this.pos = pos;
			this.direction = direction;
			this.destination = destination;
			//this.destinationDoor = destination;
		}
		
		public int[] getPosition() {
			return pos;
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
		
		/*public int getDestinationDoor() {
			return destinationDoor;
		}
		
		public void setDestinationDoor(int destinationDoor) {
			this.destinationDoor = destinationDoor;
		}*/
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
	
	/*public static class Tile {
		private int x, y;
		private int colorIndex;
		private int color;
		
		
	}*/
	
	public static class Room {
		private int number;
		// TODO: Change this to List of Tile objects
		public List<int[]> tiles; // List of tile coordinates within this room
		public List<Door> doors;
		public List<Item> items;
		private int type;
		private int colorIndex;
		private int color;
		
		public Room(int number, List<int[]> tiles, List<Door> doors, List<Item> items, int type, int colorIndex, int color) {
			this.number = number;
			this.tiles = tiles;
			this.type = type;
			this.colorIndex = colorIndex;
			this.color = color;
			this.doors = doors;
			this.items = items;
		}

		
		public int getNumber() {
			return this.number;
		}
		
		public List<int[]> getTiles() {
			return this.tiles;
		}
		
		public List<Door> getDoors() {
			return this.doors;
		}
		
		public List<Item> getItems() {
			return this.items;
		}
		
		public int getType() {
			return this.type;
		}
		
		public int getColorIndex() {
			return this.colorIndex;
		}
		
		public int getColor() {
			return this.color;
		}
	}

	/* Main room manager class */
	public static class RoomManager {
		protected int[] currentPos; /* Current x and y tile coordinates of player */
		/* Positive x is right, positive y is down */
		protected int[][] grid; /* Maps tile to room number it belongs to */
		
		protected Room currentRoom;
		protected List<Room> rooms;
		
		protected List<int[]> path; /* List of tile coordinates */
		//protected List<Integer> path; /* List of room numbers */
		
		protected int lastDirection; /* Last movement direction */
		
		//protected int lastDoorIndex;
		//protected boolean preProcessed = false;
		//protected int nextType;
		//protected int nextColorIndex;
		
		protected String[] prompts;
		
		//protected Random randomGenerator;
		
		public RoomManager() {
			grid = RoomUtility.generateGrid();
			rooms = RoomUtility.generateRooms(grid);
			path = new ArrayList<int[]>();
			
			/*randomGenerator = new Random();
			int type = randomGenerator.nextInt(TYPES.length);
			int colorIndex = randomGenerator.nextInt(COLORS.length);
			int color = COLORS[colorIndex];
			currentRoom = new Room(0, 0, 0, type, colorIndex, color);*/
			
			currentPos = new int[] { GRID_SIZE / 2, GRID_SIZE / 2 };
			int currentRoomNumber = grid[currentPos[0]][currentPos[1]];
			currentRoom = rooms.get(currentRoomNumber);
			for (int i = 0; i < currentRoom.tiles.size(); i++) {
				int[] tile = currentRoom.tiles.get(i);
				//Log.e("Tiles", Integer.toString(tile[0]) + " " + Integer.toString(tile[1]));
			}
			//rooms.add(currentRoom);
			path.add(currentPos);
			
			lastDirection = -1;
			
			prompts = new String[] {};
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
		
		public List<int[]> getPath() {
			return this.path;
		}
		
		public void setPath(List<int[]> path) {
			this.path = path;
		}
		
		public int getLastDirection() {
			return this.lastDirection;
		}
		
		public void setLastDirection(int lastDirection) {
			this.lastDirection = lastDirection;
		}
		
		/*public int getNextType() {
			return this.nextType;
		}
		
		public int getNextColorIndex() {
			return this.nextColorIndex;
		}*/
		
		public void setPrompts(String[] prompts) {
			this.prompts = prompts;
		}
		
		public String[] getPrompts() {
			return this.prompts;
		}
		
		// Returns true if move was made, otherwise false	
		// Turns decoupled from moves
		public boolean processMove(int direction) {
			int[] nextPos;
			
			// IMPORTANT: ROW, COL not X, Y
			switch (direction) {
				case MainActivity.LEFT:
					nextPos = new int[] { currentPos[0], currentPos[1] - 1 };
					break;
				case MainActivity.UP:
					nextPos = new int[] { currentPos[0] - 1, currentPos[1] };
					break;
				case MainActivity.RIGHT:
					nextPos = new int[] { currentPos[0], currentPos[1] + 1 };
					break;
				case MainActivity.DOWN:
					nextPos = new int[] { currentPos[0] + 1, currentPos[1] };
					break;
				default:
					nextPos = new int[2];
					break;
			}
			
			List<int[]> tiles = currentRoom.getTiles();
			
			for (int i = 0; i < tiles.size(); i++) {
				int[] tile = tiles.get(i);
				
				if (tile[0] == nextPos[0] && tile[1] == nextPos[1]) {
					currentPos = nextPos;
					path.add(nextPos);
					lastDirection = direction;
					
					return true;
				}
			}
			
			List<Door> doors = currentRoom.getDoors();
			
			for (int i = 0; i < doors.size(); i++) {
				Door door = doors.get(i);
				
				if (door.pos[0] == currentPos[0] && door.pos[1] == currentPos[1] && door.direction == direction) {
					//Log.e("Door", "ee");
					currentRoom = rooms.get(door.destination);
					currentPos = nextPos;
					path.add(nextPos);
					lastDirection = direction;
					
					return true;
				}
			}
			
			return false;
		}
	}
	
	// No difference between Impossible and Non-impossible spaces right now
	public static class ImpossibleRoomManager extends RoomManager {
		public ImpossibleRoomManager() {
			super();
		}
		
		public boolean processMove(int doorIndex) {
			return super.processMove(doorIndex);
			/*Door door = currentRoom.getDoors().get(doorIndex);
			int direction = door.getDirection();
			int destination = door.getDestination();
			
			if (destination >= 0) {
				currentRoom = rooms.get(destination);
				path.add(destination);
				lastDoorIndex = door.getDestinationDoor();
			}
			else {
				int number = rooms.size();
				int type, colorIndex;
				if (preProcessed) {
					preProcessed = false;
					type = nextType;
					colorIndex = nextColorIndex;
				}
				else {
					type = randomGenerator.nextInt(TYPES.length);
					colorIndex = randomGenerator.nextInt(COLORS.length);
				}
				int color = COLORS[colorIndex];
				Room newRoom = new Room(number, 0, 0, type, colorIndex, color);				
				List<Door> newDoors = newRoom.getDoors();
				List<Integer> possibleDoorIndices = new ArrayList<Integer> ();
				for (int i = 0; i < newDoors.size(); i++) {
					if ((newDoors.get(i).getDirection() + 2) % 4 == direction) {
						possibleDoorIndices.add(i);
					}
				}
				int newDoorIndex = possibleDoorIndices.get(randomGenerator.nextInt(possibleDoorIndices.size()));

				rooms.add(newRoom);
				door.setDestination(number);
				door.setDestinationDoor(newDoorIndex);
				currentRoom = newRoom;
				
				path.add(number);
				
				lastDoorIndex = newDoorIndex;
			}*/
		}
	}
	
	public static class NonImpossibleRoomManager extends RoomManager {
		public NonImpossibleRoomManager() {
			super();
		}
		
		public boolean processMove(int doorIndex) {
			return super.processMove(doorIndex);
			/*Door door = currentRoom.getDoors().get(doorIndex);
			int direction = door.getDirection();
			int destination = door.getDestination();
			
			if (destination >= 0) {
				currentRoom = rooms.get(destination);
				path.add(destination);
				lastDoorIndex = door.getDestinationDoor();
			}
			else {
				int number = rooms.size();
				int type, colorIndex;
				if (preProcessed) {
					preProcessed = false;
					type = nextType;
					colorIndex = nextColorIndex;
				}
				else {
					type = randomGenerator.nextInt(TYPES.length);
					colorIndex = randomGenerator.nextInt(COLORS.length);
				}
				int color = COLORS[colorIndex];
				Room newRoom = new Room(number, 0, 0, type, colorIndex, color);				
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
			}*/
		}
	}
}
