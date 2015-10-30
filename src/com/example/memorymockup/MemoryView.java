package com.example.memorymockup;


import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;

import com.example.memorymockup.DrawUtility.MyGLRenderer;
import com.example.memorymockup.DrawUtility.Quadrangle;
import com.example.memorymockup.RoomUtility.ImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.NonImpossibleRoomManager;
import com.example.memorymockup.RoomUtility.Room;
import com.example.memorymockup.RoomUtility.RoomManager;
import com.google.gson.Gson;

public class MemoryView extends GLSurfaceView {
	
	private MyGLRenderer mRenderer;
	
	/*public static final int centerX = 550;
	public static final int centerY = 700;
	public static final int tileSize = 140;
	public static final int tileGridSize = 6;
	
	public static final String[] roomFileNames =
		{"room1.png", "room2.png", "room3.png", "room4.png", "room5.png", "room6.png", "room7.png"};
	
	public static final double transitionMaxSpeed = 25.00;
	public static final double blurMaxSpeed = 50.00;
	*/
	public MainActivity mainActivity;
	
	public int offset;
	public AssetManager assetManager;
	public Display display;
	
	public int screenX;
	public int screenY;
	
	public Quadrangle[] roomSprites;
	public Quadrangle[] roomSpritesNext;
	public Quadrangle[] doorSprites;/*	
	public PlayerSprite playerSprite;
	
	public Paint[] roomPaints;
	public Paint doorPaintLast;
	
	public Camera camera;*/
	
	public Handler handler;
	public Runnable runnable;
	
	public int mode;
	public int task;
	
	public static int IMPOSSIBLE = 0;
	public static int NONIMPOSSIBLE = 1;
	
	public static int MATCH = 0;
	public static int ENTRY = 1;
	
	public RoomManager roomManager;
	
	public Gson gson;
	
	public double maxSpeed;
	
	// Only correct once at least one move is made
	// Don't use for purposes other than transitions
	public int previousMove;
	public Room previousRoom;
	
	//Direction player is facing
	public int playerDirection;
	
	// Just for matching
	public List<int[]> pathToFollow;
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
	
	/*public Bitmap readBitmap(String fileName) {
		try {
			InputStream inputStream = assetManager.open(fileName);
			Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
			inputStream.close();
			return bitmap;
		}
		catch (IOException e) {
			return null;
		}
	}*/
	/*
	public void initDrawables() {
		screenX = getWidth();
		screenY = getHeight();
		
		// Initialize room sprites
		roomSprites = new RoomSprite[RoomUtility.TYPES.length];
		roomSpritesNext = new RoomSprite[RoomUtility.TYPES.length];
		for (int i = 0; i < roomSprites.length; i++) {
			Bitmap roomBitmap = readBitmap(roomFileNames[i]);
			int imageX = roomBitmap.getHeight();
			int imageY = roomBitmap.getWidth();
			Matrix roomMatrix = new Matrix();
	        roomMatrix.postRotate(90);
	        roomMatrix.postScale(((float) screenX / imageX), ((float) (screenY - offset) / imageY));
	        
	        roomBitmap = Bitmap.createBitmap(roomBitmap, 0, 0, roomBitmap.getWidth(), roomBitmap.getHeight(), roomMatrix, true);
			roomSprites[i] = new RoomSprite(roomBitmap);
			roomSprites[i].position = new double[] {roomSprites[i].spriteDim[0] / 2, roomSprites[i].spriteDim[1] / 2 + offset};
			roomSpritesNext[i] = new RoomSprite(roomBitmap);
			roomSpritesNext[i].position = new double[] {roomSpritesNext[i].spriteDim[0] / 2, roomSpritesNext[i].spriteDim[1] / 2 + offset};
		}
		
		// Initialize door sprites
		doorSprites = new DoorSprite[4];
		Bitmap doorBitmap = readBitmap("door.png");
		for (int i = 0; i < doorSprites.length; i++) {
			Matrix doorMatrix = new Matrix();
	        doorMatrix.postRotate(90 * (1 - i));
	        
	        doorBitmap = Bitmap.createBitmap(doorBitmap, 0, 0, doorBitmap.getWidth(), doorBitmap.getHeight(), doorMatrix, true);
			doorSprites[i] = new DoorSprite(doorBitmap);
			doorSprites[i].setDirection(i);
			
			switch (i) {
				case 0:
					doorSprites[i].position = new double[] {doorSprites[i].spriteDim[0] / 2,
														    roomSprites[0].spriteDim[1] / 2 + offset};
					break;
				case 1:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] / 2,
														    doorSprites[i].spriteDim[1] / 2 + offset};
					break;
				case 2:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] - doorSprites[i].spriteDim[0] / 2,
							 							    roomSprites[0].spriteDim[1] / 2 + offset};
					break;
				case 3:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] / 2,
							 							    roomSprites[0].spriteDim[1] - doorSprites[i].spriteDim[1] / 2 + offset};
					break;
				default:
					break;
			}
		}
		
		//Initialize player sprite
		Bitmap playerBitmap = readBitmap("player.png");
		Matrix playerMatrix = new Matrix();
		playerMatrix.postScale(2f, 2f);
        
		playerBitmap = Bitmap.createBitmap(playerBitmap, 0, 0, playerBitmap.getWidth(), playerBitmap.getHeight(), playerMatrix, true);
		playerSprite = new PlayerSprite(playerBitmap, new int[] {3, 4}, this);
		playerSprite.position = new double[] {roomSprites[0].spriteDim[0] / 2, roomSprites[0].spriteDim[1] / 2 + offset};
		
		// Initialize paints
		roomPaints = new Paint[RoomUtility.COLORS.length];
		for (int i = 0; i < RoomUtility.COLORS.length; i++) {
			Paint paint = new Paint();
			ColorFilter colorFilter = new LightingColorFilter(RoomUtility.COLORS[i], 1);
			paint.setColorFilter(colorFilter);
			roomPaints[i] = paint;
		}
		
		doorPaintLast = new Paint(); 
		ColorFilter colorFilter = new LightingColorFilter(0xFFFFFF00, 1);
		doorPaintLast.setColorFilter(colorFilter);
		
	    camera = new Camera();
	    camera.save();
	}*/
	/*
	public void initDrawables () {
		// Initialize room textures
		roomTextures = new Quadrangle[RoomUtility.TYPES.length];
		for (int i = 0; i < roomSprites.length; i++) {
			Bitmap roomBitmap = readBitmap(roomFileNames[i]);
			int imageX = roomBitmap.getHeight();
			int imageY = roomBitmap.getWidth();
			Matrix roomMatrix = new Matrix();
	        roomMatrix.postRotate(90);
	        roomMatrix.postScale(((float) screenX / imageX), ((float) (screenY - offset) / imageY));
	        
	        roomBitmap = Bitmap.createBitmap(roomBitmap, 0, 0, roomBitmap.getWidth(), roomBitmap.getHeight(), roomMatrix, true);
			roomSprites[i] = new Quadrangle( new float[] {
				-0.5f, 0.5f, -0.5f,
				-0.5f, -0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
				0.5f, 0.5f, -0.5f
			});
			roomSprites[i].position = new double[] {roomSprites[i].spriteDim[0] / 2, roomSprites[i].spriteDim[1] / 2 + offset};
			roomSpritesNext[i] = new RoomSprite(roomBitmap);
			roomSpritesNext[i].position = new double[] {roomSpritesNext[i].spriteDim[0] / 2, roomSpritesNext[i].spriteDim[1] / 2 + offset};
		}*/
		
		// Initialize door sprites
		/*doorSprites = new DoorSprite[4];
		Bitmap doorBitmap = readBitmap("door.png");
		for (int i = 0; i < doorSprites.length; i++) {
			Matrix doorMatrix = new Matrix();
	        doorMatrix.postRotate(90 * (1 - i));
	        
	        doorBitmap = Bitmap.createBitmap(doorBitmap, 0, 0, doorBitmap.getWidth(), doorBitmap.getHeight(), doorMatrix, true);
			doorSprites[i] = new DoorSprite(doorBitmap);
			doorSprites[i].setDirection(i);
			
			switch (i) {
				case 0:
					doorSprites[i].position = new double[] {doorSprites[i].spriteDim[0] / 2,
														    roomSprites[0].spriteDim[1] / 2 + offset};
					break;
				case 1:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] / 2,
														    doorSprites[i].spriteDim[1] / 2 + offset};
					break;
				case 2:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] - doorSprites[i].spriteDim[0] / 2,
							 							    roomSprites[0].spriteDim[1] / 2 + offset};
					break;
				case 3:
					doorSprites[i].position = new double[] {roomSprites[0].spriteDim[0] / 2,
							 							    roomSprites[0].spriteDim[1] - doorSprites[i].spriteDim[1] / 2 + offset};
					break;
				default:
					break;
			}
		}
	}*/
	
	public void initMemoryView(Context context) {
		mainActivity = (MainActivity) context;
	
		// Initialize the sprites for the different entities
		offset = 0;
		//assetManager = context.getAssets();
		
		display = mainActivity.getWindowManager().getDefaultDisplay();
		
	    // Initialize the handler for the animation
	    handler = new Handler();
	    runnable = new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		};
		
	    mode = IMPOSSIBLE;
	    
	    gson = new Gson();
	    
	    previousMove = -1;
		previousRoom = null;
		
		playerDirection = 1;
		
		// Create an OpenGL ES 2.0 context
		//initDrawables();
		
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();
        mRenderer.mainActivity = mainActivity;
        
        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);
        /*mRenderer.currentShapes.add(new Triangle (new float[] {   // in counterclockwise order:
	             1.0f,  0.622008459f, 0.0f, // top
	             -0.5f, -0.311004243f, 0.0f, // bottom left
	              0.5f, -0.311004243f, 0.0f  // bottom right
	    }));*/
        //requestRender();
        //mRenderer.currentShapes.add(roomSprites[0]);
        /*mRenderer.currentShapes.add(new Triangle (new float[] {   // in counterclockwise order:
		             0.0f,  0.622008459f, 0.0f, // top
		             -0.5f, -0.311004243f, 0.0f, // bottom left
		              0.5f, -0.311004243f, 0.0f  // bottom right
		    }));*/
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        
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
	
	/*@Override
	protected void onDraw(Canvas canvas) {
		if (!mainActivity.inTransition && !mainActivity.inBlur) {
			camera.applyToCanvas(canvas);
			
			Room currentRoom = roomManager.getCurrentRoom();
			RoomSprite currentRoomSprite = roomSprites[currentRoom.getType()];
			currentRoomSprite.draw(canvas, roomPaints[currentRoom.getColorIndex()]);
			
			playerSprite.draw(canvas);
		}
		else {//if (mainActivity.inTransition) {
			switch (previousMove) {
				case 0:
				case 2:
					camera.translate((float) -playerSprite.velocity[0], 0f, 0f);
					break;
				case 1:
				case 3:
					camera.translate(0f, (float) playerSprite.velocity[1], 0f);
					break;
				default:
					break;
			}
			
			camera.applyToCanvas(canvas);
			
			RoomSprite currentRoomSprite = roomSprites[previousRoom.getType()];
			currentRoomSprite.draw(canvas, roomPaints[previousRoom.getColorIndex()]);
			
			Room currentRoom = roomManager.getCurrentRoom();
			RoomSprite nextRoomSprite = roomSpritesNext[currentRoom.getType()];
			nextRoomSprite.draw(canvas, roomPaints[currentRoom.getColorIndex()]);
			
			int status = 0;
			DoorSprite leaveDoor = doorSprites[previousMove];

			// 0 = In room, 1 = In door, 2 = Finished
			switch (previousMove) {
				case 0:
					if ((playerSprite.position[0] <= leaveDoor.position[0]) &&
						(playerSprite.position[0] >= nextRoomSprite.position[0] - 
						(leaveDoor.position[0] - currentRoomSprite.position[0]))) {
						status = 1;
					}
					else if (playerSprite.position[0] <= nextRoomSprite.position[0] + maxSpeed) {
						status = 2;
					}
					break;
				case 1:
					if ((playerSprite.position[1] <= leaveDoor.position[1]) &&
						(playerSprite.position[1] >= nextRoomSprite.position[1] - 
						(leaveDoor.position[1] - currentRoomSprite.position[1]))) {
						status = 1;
					}
					else if (playerSprite.position[1] <= nextRoomSprite.position[1] + maxSpeed) {
						status = 2;
					}
					break;
				case 2:
					if ((playerSprite.position[0] >= leaveDoor.position[0]) &&
						(playerSprite.position[0] <= nextRoomSprite.position[0] - 
						(leaveDoor.position[0] - currentRoomSprite.position[0]))) {
						status = 1;
					}
					else if (playerSprite.position[0] >= nextRoomSprite.position[0] - maxSpeed) {
						status = 2;
					}
					break;
				case 3:
					if ((playerSprite.position[1] >= leaveDoor.position[1]) &&
						(playerSprite.position[1] <= nextRoomSprite.position[1] - 
						(leaveDoor.position[1] - currentRoomSprite.position[1]))) {
						status = 1;
					}
					else if (playerSprite.position[1] >= nextRoomSprite.position[1] - maxSpeed) {
						status = 2;
					}
					break;
				default:
					break;
			}
			
			if (status == 0) {
				playerSprite.draw(canvas);
				handler.postDelayed(runnable, 1000/25);
			}
			else if (status == 1) {
				playerSprite.update();
				handler.postDelayed(runnable, 1000/25);
			}
			else if (status == 2) {
	    		playerSprite.draw(canvas);

	    		mainActivity.inTransition = false;
	    		mainActivity.inBlur = false;
	    		
	    		nextRoomSprite.directionTranslate((previousMove + 2) % 4, screenX, screenY - offset);
	    		
	    		camera.restore();
	    		camera.save();
	    		
	    		playerSprite.velocity = new double[] {0.0, 0.0};
	    		
	    		playerSprite.position = new double[] {currentRoomSprite.position[0],
						  							  currentRoomSprite.position[1]};
	    		invalidate();
	    		
	    		if (mainActivity.inShow) {
	    			mainActivity.randomPlayer.showCallback();
	    		}
	    	}
		}
	 	//else if (mainActivity.inBlur) {
			
		//}
	}*/
	
	public void showMinimap() {
		mainActivity.minimapViewFlipper.setDisplayedChild(0);
	}
	
	public void showStatusText() {
		mainActivity.minimapViewFlipper.setDisplayedChild(1);
	}
	
	public void showPromptText() {
		mainActivity.minimapViewFlipper.setDisplayedChild(2);
	}
	
	public void tryMoveSlow(int direction) {
		/*previousMove = direction;
		previousRoom = roomManager.getCurrentRoom();

		roomManager.processMove(previousMove);
		mainActivity.mapView.drawMap(previousMove);
		
		Room currentRoom = roomManager.getCurrentRoom();
		RoomSprite roomSpriteNext = roomSpritesNext[currentRoom.getType()];
		roomSpriteNext.directionTranslate(previousMove, screenX, screenY - offset);
		
		double[] accelerationVector = new double[] {
				roomSpriteNext.position[0] - playerSprite.position[0],
				roomSpriteNext.position[1] - playerSprite.position[1]};
		double acceleration = Math.sqrt(Math.pow(accelerationVector[0], 2) + Math.pow(accelerationVector[1], 2));
		accelerationVector[0] = accelerationVector[0] / acceleration;
		accelerationVector[1] = accelerationVector[1] / acceleration;
		playerSprite.accelerationVector = accelerationVector;
		maxSpeed = transitionMaxSpeed;
		playerSprite.adjustMaxSpeed(maxSpeed);
		
		mainActivity.inTransition = true;
		invalidate();
		
		Room currentRoom = roomManager.getCurrentRoom();
		RoomSprite roomSpriteNext = roomSpritesNext[currentRoom.getType()];
		roomSpriteNext.directionTranslate(previousMove, screenX, screenY - offset);
		
		double[] accelerationVector = new double[] {
				roomSpriteNext.position[0] - playerSprite.position[0],
				roomSpriteNext.position[1] - playerSprite.position[1]};
		double acceleration = Math.sqrt(Math.pow(accelerationVector[0], 2) + Math.pow(accelerationVector[1], 2));
		accelerationVector[0] = accelerationVector[0] / acceleration;
		accelerationVector[1] = accelerationVector[1] / acceleration;
		playerSprite.accelerationVector = accelerationVector;
		maxSpeed = transitionMaxSpeed;
		playerSprite.adjustMaxSpeed(maxSpeed);
		
		mainActivity.inTransition = true;*/
	}
	
	public void tryMoveSlow(int x, int y) {
		/*float[] position = new float[] {x, y};
		int direction = -1;
		boolean willMove = false;
		
		for (int i = 0; i < doorSprites.length; i++) {
			int[] dimensions = doorSprites[i].spriteDim;
			
			// Need to account for the height of the minimap in the y-direction
			if ((doorSprites[i].position[0] - dimensions[0] <= position[0]) &&
				(doorSprites[i].position[0] + dimensions[0] >= position[0]) &&
				(doorSprites[i].position[1] - dimensions[1] <= position[1]) &&
				(doorSprites[i].position[1] + dimensions[1] >= position[1])) {
				direction = i;
				willMove = true;
			}
		}
		
		if (willMove) {
			tryMoveSlow(direction);
		}*/
	}
	
	/*public void tryMoveFast(int direction) {
		//Direction player is facing
				public int direction;
		// Make this into a function
		if (mainActivity.inBlur) {
			handler.removeCallbacks(runnable);
			
			mainActivity.inTransition = false;
    		mainActivity.inBlur = false;
    		
    		//RoomSprite currentRoomSprite = roomSprites[previousRoom.getType()];
    		
    		//Room currentRoom = roomManager.getCurrentRoom();
			//RoomSprite nextRoomSprite = roomSpritesNext[currentRoom.getType()];
			
    		//nextRoomSprite.directionTranslate((previousMove + 2) % 4, screenX, screenY - offset);
    		
    		//camera.restore();
    		//camera.save();
    		
    		mRenderer.position = new float[] {0, 0, 0};
    		mRenderer.velocity = new float[] {0, 0, 0};
    		
    		//playerSprite.position = new double[] {currentRoomSprite.position[0],
				//	  							  currentRoomSprite.position[1]};
		}
		
		previousMove = direction;
		previousRoom = roomManager.getCurrentRoom();
		
		roomManager.processMove(previousMove);
		mainActivity.mapView.drawMap(previousMove);
		
		//Room currentRoom = roomManager.getCurrentRoom();
		//RoomSprite roomSpriteNext = roomSpritesNext[currentRoom.getType()];
		//roomSpriteNext.directionTranslate(previousMove, screenX, screenY - offset);
		
		//double[] accelerationVector = new double[] {
				//roomSpriteNext.position[0] - playerSprite.position[0],
				//roomSpriteNext.position[1] - playerSprite.position[1]};
		//double acceleration = Math.sqrt(Math.pow(accelerationVector[0], 2) + Math.pow(accelerationVector[1], 2));
		//accelerationVector[0] = accelerationVector[0] / acceleration;
		//accelerationVector[1] = accelerationVector[1] / acceleration;
		//playerSprite.accelerationVector = accelerationVector;
		//maxSpeed = blurMaxSpeed;
		//playerSprite.adjustMaxSpeed(maxSpeed);
		
		switch(previousMove) {
			case 0:
				mRenderer.velocity = new float [] { -1, 0, 0 };
				break;
			case 1:
				mRenderer.velocity = new float [] { 0, 0, -1 };
				break;
			case 2:
				mRenderer.velocity = new float [] { 1, 0, 0 };
				break;
			case 3:
				mRenderer.velocity = new float [] { 0, 0, 1 };
				break;
		}
		
		mainActivity.inBlur = true;
		
		//invalidate();
	}*/
	
	// No camera turn
	/*public void tryMoveFast(int direction) {
		// Make this into a function
		if (mainActivity.inBlur) {
			handler.removeCallbacks(runnable);
			
			mainActivity.inTransition = false;
    		mainActivity.inBlur = false;
    		
    		mRenderer.position = new float[] {0, 0, 0};
    		mRenderer.velocity = new float[] {0, 0, 0};
		}
		
		previousMove = direction;
		previousRoom = roomManager.getCurrentRoom();
		
		roomManager.processMove(previousMove);
		mainActivity.mapView.drawMap(previousMove);
		
		switch(previousMove) {
			case 0:
				mRenderer.velocity = new float [] { -1, 0, 0 };
				break;
			case 1:
				mRenderer.velocity = new float [] { 0, 0, -1 };
				break;
			case 2:
				mRenderer.velocity = new float [] { 1, 0, 0 };
				break;
			case 3:
				mRenderer.velocity = new float [] { 0, 0, 1 };
				break;
		}
		
		mainActivity.inBlur = true;
	}*/
	
	// Turns coupled with moves
	/*public void tryMoveFast(int direction) {
		int[] tempCurrentPos = roomManager.currentPos;
		Room tempPreviousRoom = roomManager.getCurrentRoom();
		int tempPlayerDirection = ((playerDirection + direction - 1) % 4 + 4) % 4;
		
		if (!roomManager.processMove(tempPlayerDirection)) {
			return ;
		}
		//Log.e("eeeddddd", Integer.toString(tempPlayerDirection));
		//Log.e("row", Integer.toString(roomManager.currentPos[0]));
		//Log.e("col", Integer.toString(roomManager.currentPos[1]));
		//Log.e("Room", Integer.toString(roomManager.currentRoom.getNumber()));
		
		// Make this into a function
		if (mainActivity.inBlur) {
			handler.removeCallbacks(runnable);
			
			mainActivity.inTransition = false;
    		mainActivity.inBlur = false;
    		
    		mRenderer.desiredRotationAngle = (mRenderer.desiredRotationAngle % 360 + 360) % 360;
    		mRenderer.rotationAngle = mRenderer.desiredRotationAngle;
    		mRenderer.position = DrawUtility.getDrawPos(tempCurrentPos);
    		mRenderer.velocity = new float[] {0, 0, 0};
		}
		
		previousMove = direction;
		previousRoom = tempPreviousRoom;
		playerDirection = tempPlayerDirection;
		
		mainActivity.mapView.drawMap(playerDirection);
		
		switch(previousMove) {
			case 0:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle - 90;
				break;
			case 1:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle;
				break;
			case 2:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle + 90;
				break;
			case 3:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle + 180;
				break;
		}
		
		switch(playerDirection) {
			case 0:
				mRenderer.velocity = new float [] { -1, 0, 0 };
				break;
			case 1:
				mRenderer.velocity = new float [] { 0, 0, -1 };
				break;
			case 2:
				mRenderer.velocity = new float [] { 1, 0, 0 };
				break;
			case 3:
				mRenderer.velocity = new float [] { 0, 0, 1 };
				break;
		}
		
		mainActivity.inBlur = true;
	}*/
	
	// Turns decoupled from moves
	public void tryMoveFast(int direction) {
		int[] tempCurrentPos = roomManager.currentPos;
		Room tempPreviousRoom = roomManager.getCurrentRoom();
		int tempPlayerDirection = ((playerDirection + direction - 1) % 4 + 4) % 4;
		
		if (direction == MainActivity.UP) {
			if (!roomManager.processMove(tempPlayerDirection)) {
				return ;
			}
		}
		else {
			roomManager.lastDirection = tempPlayerDirection;
		}
		
		// Make this into a function
		if (mainActivity.inBlur) {
			handler.removeCallbacks(runnable);
			
			mainActivity.inTransition = false;
    		mainActivity.inBlur = false;
    		
    		mRenderer.desiredRotationAngle = (mRenderer.desiredRotationAngle % 360 + 360) % 360;
    		mRenderer.rotationAngle = mRenderer.desiredRotationAngle;
    		mRenderer.position = DrawUtility.getDrawPos(tempCurrentPos);
    		mRenderer.velocity = new float[] {0, 0, 0};
		}
		
		previousMove = direction;
		previousRoom = tempPreviousRoom;
		playerDirection = tempPlayerDirection;
		
		if (direction == MainActivity.UP) {
			mainActivity.mapView.drawMap(playerDirection);
		}
		
		switch(previousMove) {
			case 0:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle - 90;
				break;
			case 1:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle;
				break;
			case 2:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle + 90;
				break;
			case 3:
				mRenderer.desiredRotationAngle = mRenderer.rotationAngle + 180;
				break;
		}
		
		switch(playerDirection) {
			case 0:
				mRenderer.velocity = new float [] { -1, 0, 0 };
				break;
			case 1:
				mRenderer.velocity = new float [] { 0, 0, -1 };
				break;
			case 2:
				mRenderer.velocity = new float [] { 1, 0, 0 };
				break;
			case 3:
				mRenderer.velocity = new float [] { 0, 0, 1 };
				break;
		}
		
		mainActivity.inBlur = true;
	}
	
	
	
	public void setRoomManager(String roomInfo) {
		if (this.mode == IMPOSSIBLE) {
			this.roomManager = gson.fromJson(roomInfo, ImpossibleRoomManager.class);
		}
		else {
			this.roomManager = gson.fromJson(roomInfo, NonImpossibleRoomManager.class);
		}
		
		int[][] grid = this.roomManager.grid;
		int[] currentPos = new int[] { grid.length / 2, grid[0].length / 2 };
		this.roomManager.currentPos = currentPos;
		
		int roomNumber = grid[currentPos[0]][currentPos[1]];
		Room room = this.roomManager.getRooms().get(roomNumber);
		this.roomManager.setCurrentRoom(room);
		
		pathToFollow = this.roomManager.getPath();
		List<int[]> newPath = new ArrayList<int[]>();
		newPath.add(currentPos);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDirection(-1);
	}
	
	//public void setPathToFollow(List<Integer> pathToFollow) {
		//this.pathToFollow = pathToFollow;
	//}
	
	public boolean matchPath() {
		List<int[]> pathUntilNow = roomManager.getPath();
		
		if (pathUntilNow.size() == pathToFollow.size()) {
			for (int i = 0; i < pathUntilNow.size(); i++) {
				int[] untilPos = pathUntilNow.get(i);
				int[] followPos = pathToFollow.get(i);
				
				if (untilPos[0] != followPos[0] || untilPos[1] != followPos[1]) {
					status = "Path is incorrect.";
					mainActivity.statusText.setText(status);
					showStatusText();
					return false;
				}
			}
			
			status = "Path is correct.";
			mainActivity.statusText.setText(status);
			showStatusText();
			return true;
		}
		
		status = "Path is incorrect.";
		mainActivity.statusText.setText(status);
		showStatusText();
		return false;
	}
	
	public String getRoomInfoStringified() {
		return gson.toJson(roomManager);
	}
	/*
	public void resetPath() {
		if (mainActivity.inBlur) {
			handler.removeCallbacks(runnable);
			
			mainActivity.inTransition = false;
    		mainActivity.inBlur = false;
    		
    		RoomSprite currentRoomSprite = roomSprites[previousRoom.getType()];
    		
    		Room currentRoom = roomManager.getCurrentRoom();
			RoomSprite nextRoomSprite = roomSpritesNext[currentRoom.getType()];
			
    		nextRoomSprite.directionTranslate((previousMove + 2) % 4, screenX, screenY - offset);
    		
    		camera.restore();
    		camera.save();
    		
    		playerSprite.velocity = new double[] {0.0, 0.0};
    		
    		playerSprite.position = new double[] {currentRoomSprite.position[0],
					  							  currentRoomSprite.position[1]};
		}
		
		previousMove = -1;
		previousRoom = null;
		
		Room room = this.roomManager.getRooms().get(0);
		this.roomManager.setCurrentRoom(room);
		List<Integer> newPath = new ArrayList<Integer>();
		newPath.add(0);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDoorIndex(-1);
		playerSprite.position = new double[] {roomSprites[room.getType()].spriteDim[0] / 2, 
										      roomSprites[room.getType()].spriteDim[1] / 2 + offset};
		playerSprite.row = 0;
		//mainActivity.minimap.removeAllViews();
		showMinimap();
		mainActivity.mapView.resetMap();
		invalidate();
		mainActivity.inTransition = false;
	}*/
	
	public void resetPath() {
		if (mainActivity.inBlur) {
			mainActivity.inTransition = false;
    		mainActivity.inBlur = false;
    		
    		mRenderer.position = DrawUtility.getDrawPos(new int[] { RoomUtility.GRID_SIZE / 2, RoomUtility.GRID_SIZE / 2 });
    		mRenderer.velocity = new float[] {0, 0, 0};
		}
		
		previousMove = -1;
		previousRoom = null;
		
		playerDirection = 1;
		
		int[][] grid = this.roomManager.grid;
		int[] currentPos = new int[] { grid.length / 2, grid[0].length / 2 };
		this.roomManager.currentPos = currentPos;
		
		int roomNumber = grid[currentPos[0]][currentPos[1]];
		Room room = this.roomManager.getRooms().get(roomNumber);
		this.roomManager.setCurrentRoom(room);
		
		List<int[]> newPath = new ArrayList<int[]>();
		newPath.add(currentPos);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDirection(-1);
		
		/*Room room = this.roomManager.getRooms().get(0);
		this.roomManager.setCurrentRoom(room);
		List<Integer> newPath = new ArrayList<Integer>();
		newPath.add(0);
		this.roomManager.setPath(newPath);
		this.roomManager.setLastDoorIndex(-1);*/
		//mainActivity.minimap.removeAllViews();
		showMinimap();
		mainActivity.mapView.resetMap();
		invalidate();
		mainActivity.inTransition = false;
	}
}