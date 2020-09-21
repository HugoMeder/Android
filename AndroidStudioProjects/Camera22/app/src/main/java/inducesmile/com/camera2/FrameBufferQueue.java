package inducesmile.com.camera2;

import android.util.Log;

import java.util.Vector;

public class FrameBufferQueue {

    private Vector<JPegFrameBuffer> recycleBuffer = new Vector<JPegFrameBuffer>() ;
    private Object sync = new Object () ;

    public class JPegFrameBuffer {

        private byte[] buffer;
        private int width;
        private int height;
        private int bufferSize;

        public JPegFrameBuffer(int bufferSize, int width, int height) {
            log ( "alloc "+bufferSize )  ;
            this.buffer = new byte[bufferSize*2] ;
            this.width = width ;
            this.height = height ;
            this.bufferSize = bufferSize ;
        }

        private void log ( String line ) {
            Log.i ( "JPegFrameBuffer", line ) ;
        }
        public void realloc(int bufferSize, int width, int height) {

            this.width = width ;
            this.height = height ;
            this.bufferSize = bufferSize ;
            if ( buffer.length < bufferSize ) {
                log ( "realloc, old "+buffer.length+", new "+bufferSize )  ;
                buffer = new byte[bufferSize];
            } else {
                log ( "recycle "+buffer.length )  ;
            }
        }

        public byte[] getBuffer () {
            return buffer ;
        }

        public int getWidht () {
            return width ;
        }

        public int getHeight () {
            return height ;
        }

        public int getBufferSizes () {
            return bufferSize ;
        }

        public void release () {
            log ( "release "+buffer.length ) ;
            synchronized ( sync ) {
                recycleBuffer.add ( this ) ;
            }
        }
    }

    public JPegFrameBuffer getBuffer ( int bufferSize, int width, int height ) {
        JPegFrameBuffer rv = null ;
        synchronized ( sync ) {
            if ( recycleBuffer.size() > 0 ) {
                rv = recycleBuffer.get( 0 ) ;
                recycleBuffer.remove( 0 ) ;
                rv.realloc ( bufferSize, width, height ) ;
             } else {
                rv = new JPegFrameBuffer ( bufferSize, width, height ) ;
            }
            return rv ;
        }
    }
}
