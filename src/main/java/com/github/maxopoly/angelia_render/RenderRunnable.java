package com.github.maxopoly.angelia_render;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

import com.github.maxopoly.angeliacore.model.location.Location;
import java.nio.IntBuffer;
import org.apache.logging.log4j.Logger;
import org.lwjgl.Version;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;

public class RenderRunnable implements Runnable {

    private Logger logger;
    private VBOHandler vboHandler;

    private long window;

    private float cameraX, cameraY, cameraZ, cameraRotX = 0, cameraRotY = 0, cameraRotZ = 0;


    public RenderRunnable(Logger logger, VBOHandler vboHandler) {
        this.logger = logger;
        this.vboHandler = vboHandler;
    }

    @Override
    public void run() {
        logger.info("Loading LWJGL with version: " + Version.getVersion());

        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    public void updateCameraLocation(Location loc) {
        this.cameraX = (float) loc.getX();
        this.cameraY = (float) loc.getY();
        this.cameraZ = (float) loc.getZ();
    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(800, 800, "Hello World!", NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
            if (key == GLFW_KEY_W) {
                cameraZ -= 1;
            } else if (key == GLFW_KEY_S) {
                cameraZ += 1;
            }
            if (key == GLFW_KEY_A) {
                cameraX += 1;
            } else if (key == GLFW_KEY_D) {
                cameraX -= 1;
            }

            if (key == GLFW_KEY_SPACE) {
                cameraY += 1;
            } else if (key == GLFW_KEY_LEFT_SHIFT) {
                cameraY -= 1;
            }

            if (key == GLFW_KEY_Q) {
                cameraRotX += 1;
            } else if (key == GLFW_KEY_E) {
                cameraRotX -= 1;
            }

            if (key == GLFW_KEY_R) {
                cameraRotY += 1;
            } else if (key == GLFW_KEY_F) {
                cameraRotY -= 5;
            }

            if (key == GLFW_KEY_T) {
                cameraRotZ += 5;
            } else if (key == GLFW_KEY_G) {
                cameraRotZ -= 5;
            }
        });

        // Get the thread stack and push a new frame
        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(window, (vidmode.width() - pWidth.get(0)) / 2, (vidmode.height() - pHeight.get(0)) / 2);
        } // the stack frame is popped automatically

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        //allow actually doing stuff
        GL.createCapabilities();
    }

    private void loop() {
        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the GLCapabilities instance and makes the OpenGL
        // bindings available for use.

        // Set the clear color
        glClearColor(0.529f, 0.808f, 0.922f, 0.0f);

        GL11.glMatrixMode(GL11.GL_PROJECTION);

        GL11.glLoadIdentity();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);

        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            GL11.glLoadIdentity();

            GL11.glFrustum(-1f, 1f, -1f, 1f, 1f, 50f);

            GL11.glTranslatef(-cameraX, -cameraY, -cameraZ);
            GL11.glRotatef(cameraRotX, 1, 0, 0);
            //GL11.glRotatef(cameraRotY, 0, 1, 0);
            //GL11.glRotatef(cameraRotZ, 0, 0, 1);

            GL11.glPushMatrix();

            vboHandler.popQueue();
            vboHandler.render();


            //logger.info(String.format("Camera pos: %f, %f, %f - Camera angle: %f, %f, %f", cameraX, cameraY, cameraZ, cameraRotX, cameraRotY, cameraRotZ));

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

}
