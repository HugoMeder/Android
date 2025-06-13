package inducesmile.com.camera2;

import android.util.Log;

import java.util.Vector;

public class FrameBufferQueue {

    private Vector<FrameBuffer> recycleBuffer = new Vector<FrameBuffer>() ;
    private Object sync = new Object () ;

    public class FrameBuffer {

        private byte[] buffer;
        private int width;
        private int height;
        private int bufferSize;
        private int format;
        private int orientation;

        public FrameBuffer(int bufferSize, int width, int height, int format, int orientation ) {
            log ( "alloc "+bufferSize )  ;
            this.buffer = new byte[bufferSize] ;
            this.width = width ;
            this.height = height ;
            this.format = format ;
            this.orientation = orientation ;
            this.bufferSize = bufferSize ;
        }

        private void log ( String line ) {
            Log.i ( "JPegFrameBuffer", line ) ;
        }
        public void realloc(int bufferSize, int width, int height, int format, int orientation ) {

            this.width = width ;
            this.height = height ;
            this.format = format ;
            this.orientation = orientation ;
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

        public int getFormat () { return format ; }

        public int getOrientation () { return orientation ; }

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

    public FrameBuffer getBuffer (int bufferSize, int width, int height, int format, int orientation ) {
        FrameBuffer rv = null ;
        synchronized ( sync ) {
            if ( recycleBuffer.size() > 0 ) {
                rv = recycleBuffer.get( 0 ) ;
                recycleBuffer.remove( 0 ) ;
                rv.realloc ( bufferSize, width, height, format, orientation ) ;
             } else {
                rv = new FrameBuffer( bufferSize, width, height, format, orientation ) ;
            }
            return rv ;
        }
    }
}
