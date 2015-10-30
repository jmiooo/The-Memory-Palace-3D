package com.example.memorymockup;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.SystemClock;

import com.example.memorymockup.RoomUtility.Door;
import com.example.memorymockup.RoomUtility.Room;

// In the tile coordinate system, x is left to right, but y is top to bottom
// In the drawing coordinate system, x stays the same, but y corresponds to z
// OPENGL POSITIVE Z AXIS COMES OUT OF THE SCREEN
public class DrawUtility {
	
	public static float ROOM_SIZE = 2f;
	
	public static float[] getDrawPos (int[] tilePos) {
		return new float[] {
			tilePos[1] * ROOM_SIZE,
			0,
			tilePos[0] * ROOM_SIZE
		};
	}
	
	public static int loadTexture(final Context context, final int resourceId)
	{
	    final int[] textureHandle = new int[1];
	 
	    GLES20.glGenTextures(1, textureHandle, 0);
	 
	    if (textureHandle[0] != 0)
	    {
	        final BitmapFactory.Options options = new BitmapFactory.Options();
	        //options.inScaled = false;   // No pre-scaling
	 
	        // Read in the resource
	        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
	        
	        // Set the active texture unit to texture unit 0.
	        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
	 
	        // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
	 
	        // Set filtering
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
	 
	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
	 
	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	        bitmap.recycle();
	    }
	 
	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("Error loading texture.");
	    }
	 
	    return textureHandle[0];
	}
	
	
	public static class MyGLRenderer implements GLSurfaceView.Renderer {
		
		//public List<Shape> currentShapes = new ArrayList<Shape> ();
		
		public static float ROTATION_SPEED = 1;
		
		//Must have same length as RoomUtility.TYPES
		//The i-th element of each list corresponds to the texture used for type i
		public static int[] doorTextures =
			{ R.drawable.doorthree_a, R.drawable.doorthree_b };
		public static int[] roomTextures =
			{ R.drawable.floorthree_a, R.drawable.floorthree_b };
		public static int[] wallTextures =
			{ R.drawable.wallthree_a, R.drawable.wallthree_b };

		public MainActivity mainActivity;
		
		private Shape[] roomSprites;
		private Shape[] doorSprites;
		private Shape[] wallSprites;
		
		private int[] roomTextureHandles;
		private int[] doorTextureHandles;
		private int[] wallTextureHandles;
		
		public float rotationAngle;
		public float desiredRotationAngle;
		
		//Position and velocity for camera
		public float[] position;
		public float[] velocity;
		
		private float currentTime;
		private float lastFrameTime;
		
		private final float[] mMVPMatrix = new float[16];
		private final float[] mProjectionMatrix = new float[16];
		private final float[] mRotationMatrix = new float[16];
		private final float[] mViewMatrix = new float[16];
		
		private final float[] mTotalMatrix = new float[16];
		private final float[] mTransformMatrix = new float[16];
		
		

		public void onSurfaceCreated(GL10 unused, EGLConfig config) {
	        // Set the background frame color
	        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	        
	        GLES20.glEnable( GLES20.GL_DEPTH_TEST );
	        
	        GLES20.glEnable( GLES20.GL_CULL_FACE );
	        GLES20.glCullFace( GLES20.GL_BACK );
	        
	        //Initialize assets for drawing rooms
	        roomSprites = new Shape [] {
	        	new Quadrangle( new float[] {
					-1f, -1f, -1f,
					-1f, -1f, 1f,
					1f, -1f, 1f,
					1f, -1f, -1f
	        	}),
	        	new Quadrangle( new float[] {
					-1f, 2f, 1f,
					-1f, 2f, -1f,
					1f, 2f, -1f,
					1f, 2f, 1f
	        	})
	        };
	        //Hack to make doors appear in front of walls, try to fix
	        doorSprites = new Shape [] {
	        	new Quadrangle( new float[] {
					-0.99f, 1f, 0.5f,
					-0.99f, -1f, 0.5f,
					-0.99f, -1f, -0.5f,
					-0.99f, 1f, -0.5f
		        }),
		        new Quadrangle( new float[] {
						-0.5f, 1f, -0.99f,
						-0.5f, -1f, -0.99f,
						0.5f, -1f, -0.99f,
						0.5f, 1f, -0.99f
			    }),
			    new Quadrangle( new float[] {
						0.99f, 1f, -0.5f,
						0.99f, -1f, -0.5f,
						0.99f, -1f, 0.5f,
						0.99f, 1f, 0.5f
			    }),
			    new Quadrangle( new float[] {
			    		0.5f, 1f, 0.99f,
						0.5f, -1f, 0.99f,
						-0.5f, -1f, 0.99f,
						-0.5f, 1f, 0.99f
			    })};
	        wallSprites = new Shape [] {
	        	new Quadrangle( new float[] {
					-1f, 2f, 1f,
					-1f, -1f, 1f,
					-1f, -1f, -1f,
					-1f, 2f, -1f
		        }),
		        new Quadrangle( new float[] {
						-1f, 2f, -1f,
						-1f, -1f, -1f,
						1f, -1f, -1f,
						1f, 2f, -1f
			    }),
			    new Quadrangle( new float[] {
						1f, 2f, -1f,
						1f, -1f, -1f,
						1f, -1f, 1f,
						1f, 2f, 1f
			    }),
			    new Quadrangle( new float[] {
			    		1f, 2f, 1f,
						1f, -1f, 1f,
						-1f, -1f, 1f,
						-1f, 2f, 1f
			    })};
	        for (int i = 0; i < doorSprites.length; i++)
	        	doorSprites[i].color = new float [] { 0.8f, 0.7f, 0.55f, 1.0f };
	        
	        rotationAngle = 0;
	        desiredRotationAngle = 0;
	        
	        position = DrawUtility.getDrawPos(new int[] { RoomUtility.GRID_SIZE / 2, RoomUtility.GRID_SIZE / 2 });
	        velocity = new float [] { 0, 0, 0 };
	        
	        roomTextureHandles = new int[roomTextures.length];
    		doorTextureHandles = new int[doorTextures.length];
    		wallTextureHandles = new int[wallTextures.length];
    		
	        // Retrieve our image from resources
	        for (int i = 0; i < roomTextures.length; i++) {
	        	roomTextureHandles[i] = loadTexture(mainActivity, roomTextures[i]);
	        }
	        for (int i = 0; i < doorTextures.length; i++) {
	        	doorTextureHandles[i] = loadTexture(mainActivity, doorTextures[i]);
	        }
	        for (int i = 0; i < wallTextures.length; i++) {
	        	wallTextureHandles[i] = loadTexture(mainActivity, wallTextures[i]);
	        }
		        
	        lastFrameTime = ((float) SystemClock.uptimeMillis() / 1000);
	    }

	    public void onDrawFrame(GL10 unused) {
	    	//Log.e("e", Float.toString(velocity[0]));
	    	
	    	// Get time passed since last frame
	    	currentTime = ((float) SystemClock.uptimeMillis() / 1000);
	    	float deltaTime = currentTime - lastFrameTime;
	    	lastFrameTime = currentTime;
	    	
	        // Redraw background color
	        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
	        
	        //TODO: Rotation is very hacky, please fix
	        if (rotationAngle == desiredRotationAngle) {
	        	float[] targetPos = DrawUtility.getDrawPos(mainActivity.memoryView.roomManager.currentPos);
		        // Set the camera position (View matrix)
		        
		        if ((Math.abs(targetPos[0] - position[0]) <= Math.abs(velocity[0] * deltaTime)) &&
		        	(Math.abs(targetPos[2] - position[2]) <= Math.abs(velocity[2] * deltaTime)))
		        {
		        	position = targetPos;
		        	velocity = new float [] { 0, 0, 0 };
		        	mainActivity.inBlur = false;
		        }
		        else {
			        position = new float [] {
			        	position[0] + velocity[0] * deltaTime,
						position[1] + velocity[1] * deltaTime,
						position[2] + velocity[2] * deltaTime
					};
		        }
		        
		        //Log.e("e", Float.toString(position[0]));
		        //Log.e("e", Float.toString(z));
		        /*Matrix.setLookAtM(mViewMatrix, 0,
		        				  position[0], position[1], position[2] + 1,
		        				  position[0], position[1], position[2],
		        				  0f, 1.0f, 0.0f);
		        
		        Matrix.translateM(mViewMatrix, 0, -position[0], -position[1], -position[2]);
		        Matrix.rotateM(mViewMatrix, 0, rotationAngle, 0, 1, 0);
		        Matrix.translateM(mViewMatrix, 0, position[0], position[1], position[2]);*/
		        
		        // Change 5th, 8th value here
		        Matrix.setLookAtM(mViewMatrix, 0,
      				  0f, 0f, 0.6f,
      				  0f, 0f, 0f,
      				  0f, 1.0f, 0.0f);
      
		        Matrix.rotateM(mViewMatrix, 0, rotationAngle, 0, 1, 0);
			    Matrix.translateM(mViewMatrix, 0, -position[0], -position[1], -position[2]);
	    	}
	        else {
	        	/*Matrix.setLookAtM(mViewMatrix, 0,
      				  position[0], position[1], position[2] + 1,
      				  position[0], position[1], position[2],
      				  0f, 1.0f, 0.0f);*/
	        	
	        	// Change 5th, 8th value here
	        	Matrix.setLookAtM(mViewMatrix, 0,
	      				  0f, 0f, 0.6f,
	      				  0f, 0f, 0f,
	      				  0f, 1.0f, 0.0f);
	        	
	        	if (((rotationAngle < desiredRotationAngle) && (desiredRotationAngle - rotationAngle <= ROTATION_SPEED)) ||
	        		((rotationAngle > desiredRotationAngle) && (rotationAngle - desiredRotationAngle <= ROTATION_SPEED))) {
	        		desiredRotationAngle = (desiredRotationAngle % 360 + 360) % 360;
	        		rotationAngle = desiredRotationAngle;
	        	}
	        	else {
	        		rotationAngle += (desiredRotationAngle - rotationAngle) > 0 ? ROTATION_SPEED : -ROTATION_SPEED;
	        	}
	        	
	        	Matrix.rotateM(mViewMatrix, 0, rotationAngle, 0, 1, 0);
	        	Matrix.translateM(mViewMatrix, 0, -position[0], -position[1], -position[2]);
	        	//Log.e("333", Float.toString(rotationAngle));
	        }
	        
	        //Drawing in the current room
	        //TODO: Hacky, package this better
	        int[][] grid = mainActivity.memoryView.roomManager.grid;
	        Room room;
	        
	        if (mainActivity.inBlur) {
	        	room = mainActivity.memoryView.previousRoom;
	        }
	        else {
	        	room = mainActivity.memoryView.roomManager.currentRoom;
	        }
	        
	        int statType = room.getType();
	        int statNumber = room.getNumber();
	        
	        for (int i = 0; i < room.tiles.size(); i++) {
	        	int[] tile = room.tiles.get(i);
	        	float[] drawTilePos = getDrawPos(tile);
	        	
	        	Matrix.setIdentityM(mTransformMatrix, 0);
	 	        Matrix.translateM(mTransformMatrix, 0, drawTilePos[0], 0, drawTilePos[2]);
	 	        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
	 	        Matrix.multiplyMM(mTotalMatrix, 0, mMVPMatrix, 0, mTransformMatrix, 0);
	        	
	        	//Log.e("eee", Integer.toString(currentShapes.size()));
		        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, roomTextureHandles[statType]);
		        for (int j = 0; j < roomSprites.length; j++)
		        	roomSprites[j].draw(mTotalMatrix);
		        
		        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, wallTextureHandles[statType]);
		        for (int j = 0; j < wallSprites.length; j++) {
		        	switch (j) {
		        		case MainActivity.LEFT:
		        			if (tile[1] <= 0 || grid[tile[0]][tile[1] - 1] != statNumber)
		        				wallSprites[j].draw(mTotalMatrix);
		        			break;
		        		case MainActivity.UP:
		        			if (tile[0] <= 0 || grid[tile[0] - 1][tile[1]] != statNumber)
		        				wallSprites[j].draw(mTotalMatrix);
		        			break;
		        		case MainActivity.RIGHT:
		        			if (tile[1] >= RoomUtility.GRID_SIZE - 1 || grid[tile[0]][tile[1] + 1] != statNumber)
		        				wallSprites[j].draw(mTotalMatrix);
		        			break;
		        		case MainActivity.DOWN:
		        			if (tile[0] >= RoomUtility.GRID_SIZE - 1 || grid[tile[0] + 1][tile[1]] != statNumber)
		        				wallSprites[j].draw(mTotalMatrix);
		        			break;
		        	}
		        }
		        
		        /*GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, doorTextureHandles[statType]);
		        for (int i = 0; i < doorSprites.length; i++)
		        	doorSprites[j].draw(mMVPMatrix);*/
	        }
	        
	        for (int i = 0; i < room.doors.size(); i++) {
	        	Door door = room.doors.get(i);
	        	int[] doorPos = door.getPosition();
	        	float[] drawTilePos = getDrawPos(doorPos);
	        	
	        	Matrix.setIdentityM(mTransformMatrix, 0);
	 	        Matrix.translateM(mTransformMatrix, 0, drawTilePos[0], 0, drawTilePos[2]);
	 	        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
	 	        Matrix.multiplyMM(mTotalMatrix, 0, mMVPMatrix, 0, mTransformMatrix, 0);
		        
		        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, doorTextureHandles[statType]);
		        doorSprites[door.getDirection()].draw(mTotalMatrix);
	        }
	        
	        // Drawing in the next room
	        //TODO: Consolidate this and the above into a function
	        if (mainActivity.inBlur) {
	        	Room newRoom = mainActivity.memoryView.roomManager.currentRoom;
	        	
	        	int newType = newRoom.getType();
		        int newNumber = newRoom.getNumber();
		        
		        for (int i = 0; i < newRoom.tiles.size(); i++) {
		        	int[] tile = newRoom.tiles.get(i);
		        	float[] drawTilePos = getDrawPos(tile);
		        	
		        	Matrix.setIdentityM(mTransformMatrix, 0);
		 	        Matrix.translateM(mTransformMatrix, 0, drawTilePos[0], 0, drawTilePos[2]);
		 	        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		 	        Matrix.multiplyMM(mTotalMatrix, 0, mMVPMatrix, 0, mTransformMatrix, 0);
		        	
		        	//Log.e("eee", Integer.toString(currentShapes.size()));
			        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, roomTextureHandles[newType]);
			        for (int j = 0; j < roomSprites.length; j++)
			        	roomSprites[j].draw(mTotalMatrix);
			        
			        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, wallTextureHandles[newType]);
			        for (int j = 0; j < wallSprites.length; j++) {
			        	switch (j) {
			        		case MainActivity.LEFT:
			        			if (tile[1] <= 0 || grid[tile[0]][tile[1] - 1] != newNumber)
			        				wallSprites[j].draw(mTotalMatrix);
			        			break;
			        		case MainActivity.UP:
			        			if (tile[0] <= 0 || grid[tile[0] - 1][tile[1]] != newNumber)
			        				wallSprites[j].draw(mTotalMatrix);
			        			break;
			        		case MainActivity.RIGHT:
			        			if (tile[1] >= RoomUtility.GRID_SIZE - 1 || grid[tile[0]][tile[1] + 1] != newNumber)
			        				wallSprites[j].draw(mTotalMatrix);
			        			break;
			        		case MainActivity.DOWN:
			        			if (tile[0] >= RoomUtility.GRID_SIZE - 1 || grid[tile[0] + 1][tile[1]] != newNumber)
			        				wallSprites[j].draw(mTotalMatrix);
			        			break;
			        	}
			        }
		        }
		        
		        for (int i = 0; i < newRoom.doors.size(); i++) {
		        	Door door = newRoom.doors.get(i);
		        	int[] doorPos = door.getPosition();
		        	float[] drawTilePos = getDrawPos(doorPos);
		        	
		        	Matrix.setIdentityM(mTransformMatrix, 0);
		 	        Matrix.translateM(mTransformMatrix, 0, drawTilePos[0], 0, drawTilePos[2]);
		 	        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
		 	        Matrix.multiplyMM(mTotalMatrix, 0, mMVPMatrix, 0, mTransformMatrix, 0);
			        
			        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, doorTextureHandles[newType]);
			        doorSprites[door.getDirection()].draw(mTotalMatrix);
		        }
	        }
	    }

	    public void onSurfaceChanged(GL10 unused, int width, int height) {
	    	GLES20.glViewport(0, 0, width, height);

    	    float ratio = (float) width / height;

    	    // this projection matrix is applied to object coordinates
    	    // in the onDrawFrame() method
    	    // Change the 4th to 8th values here
    	    Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 0.5f, 9);
	    }
	    
	    public static int loadShader(int type, String shaderCode){

	        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
	        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
	        int shader = GLES20.glCreateShader(type);

	        // add the source code to the shader and compile it
	        GLES20.glShaderSource(shader, shaderCode);
	        GLES20.glCompileShader(shader);

	        return shader;
	    }
	    
	    
	}
	
	public static class Shape {
		// number of coordinates per vertex in this array
	    static final int COORDS_PER_VERTEX = 3;
	    
	    private final String vertexShaderCode =
	    		"uniform mat4 uMVPMatrix;" +
			    "attribute vec4 vPosition;" +
			    "attribute vec2 a_texCoord;" +
			    "varying vec2 v_texCoord;" +
			    "void main() {" +
			    "  gl_Position = uMVPMatrix * vPosition;" +
			    "  v_texCoord = a_texCoord;" +
			    "}";

	        // Use to access and set the view transformation

	    private final String fragmentShaderCode =
	    		"precision mediump float;" +
			    "varying vec2 v_texCoord;" +
			    "uniform sampler2D s_texture;" +
			    "void main() {" +
			    "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
			    "}";
	        
	    // Give coordinates in order: Top, Bottom left, Bottom right
		float coords[] = {};
		private FloatBuffer vertexBuffer;
		
		private float[] textureData = new float[] {
		      0.0f, 0.0f,
		      0.0f, 1.0f,
		      1.0f, 1.0f,
		      1.0f, 0.0f
		};
		private FloatBuffer textureBuffer;
		
		private final int mProgram;
		
		private int mPositionHandle;
	    private int mColorHandle;
	    private int mMVPMatrixHandle;

	    private final int vertexCount;
	    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	    
	    protected ShortBuffer drawListBuffer;
		protected short drawOrder[];

	    // Set color with red, green, blue and alpha (opacity) values
	    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

	    public Shape(float[] newCoords) {
	        // initialize vertex byte buffer for shape coordinates
	    	vertexCount = newCoords.length / COORDS_PER_VERTEX;
	    	coords = newCoords;
	        ByteBuffer bb = ByteBuffer.allocateDirect(
	                // (number of coordinate values * 4 bytes per float)
	                coords.length * 4);
	        // use the device hardware's native byte order
	        bb.order(ByteOrder.nativeOrder());

	        // create a floating point buffer from the ByteBuffer
	        vertexBuffer = bb.asFloatBuffer();
	        // add the coordinates to the FloatBuffer
	        vertexBuffer.put(coords);
	        // set the buffer to read the first coordinate
	        vertexBuffer.position(0);
	        
	        bb = ByteBuffer.allocateDirect(textureData.length * 4);
			bb.order(ByteOrder.nativeOrder());
			textureBuffer = bb.asFloatBuffer();
			textureBuffer.put(textureData);
		  	textureBuffer.position(0);
	        
	        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                    vertexShaderCode);
			int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
			                    fragmentShaderCode);
			
			// create empty OpenGL ES Program
			mProgram = GLES20.glCreateProgram();
			
			// add the vertex shader to program
			GLES20.glAttachShader(mProgram, vertexShader);
			
			// add the fragment shader to program
			GLES20.glAttachShader(mProgram, fragmentShader);
			
			// creates OpenGL ES program executables
			GLES20.glLinkProgram(mProgram);
	    }
	    

	    public void draw(float[] mvpMatrix) {
	        // Add program to OpenGL ES environment
	        GLES20.glUseProgram(mProgram);

	        // get handle to vertex shader's vPosition member
	        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

	        // Enable a handle to the triangle vertices
	        GLES20.glEnableVertexAttribArray(mPositionHandle);

	        // Prepare the triangle coordinate data
	        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	                                     GLES20.GL_FLOAT, false,
	                                     vertexStride, vertexBuffer);
	        
	        // Get handle to texture coordinates location
	        int mTexCoordLoc = GLES20.glGetAttribLocation(mProgram, 
	                             "a_texCoord" );
	       
	        // Enable generic vertex attribute array
	        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );
	       
	        // Prepare the texturecoordinates
	        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
	                      false,
	                      0, textureBuffer);

	        /*// get handle to fragment shader's vColor member
	        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

	        // Set color for drawing the triangle
	        GLES20.glUniform4fv(mColorHandle, 1, color, 0);*/
	        
	        // get handle to shape's transformation matrix
	        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

	        // Pass the projection and view transformation to the shader
	        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

	        // Get handle to textures locations
	        int mSamplerLoc = GLES20.glGetUniformLocation (mProgram, 
	                            "s_texture" );
	       
	        // Set the sampler texture unit to 0, where we have saved the texture.
	        GLES20.glUniform1i ( mSamplerLoc, 0);

	        // Draw the triangle
	        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
                    GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

	        // Disable vertex array
	        GLES20.glDisableVertexAttribArray(mPositionHandle);
	        GLES20.glDisableVertexAttribArray(mTexCoordLoc);
	    }
	    
	    void setColor(float[] color) {
	    	this.color = color;
	    }
	}
	
	/*public static class Triangle extends Shape {
		
	    public Triangle(float[] coords) {
	        super(setup(coords));
	        
	        drawOrder = new short[] { 0, 1, 2 };
	        
	        ByteBuffer dlb = ByteBuffer.allocateDirect(
            // (# of coordinate values * 2 bytes per short)
                    drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);
	    }
	    
	    public static float[] setup (float[] coords) {
	    	if (coords.length != 9)
	    		return new float[0];
	    	
	    	return coords;
	    }
	}*/
	
	public static class Quadrangle extends Shape {
	    public Quadrangle(float[] coords) {
	        super(setup(coords));
	        
	        drawOrder = new short[] { 0, 1, 2, 0, 2, 3 };
	        
	        ByteBuffer dlb = ByteBuffer.allocateDirect(
            // (# of coordinate values * 2 bytes per short)
                    drawOrder.length * 2);
            dlb.order(ByteOrder.nativeOrder());
            drawListBuffer = dlb.asShortBuffer();
            drawListBuffer.put(drawOrder);
            drawListBuffer.position(0);
	    }
	    
	    public static float[] setup (float[] coords) {
	    	if (coords.length != 12)
	    		return new float[0];
	    	
	    	return coords;
	    }
	}
}