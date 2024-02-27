package de.bitsnarts.gnsstracker;

public class Globals {

    static int instanceNr ;
    private MainActivity mainActivity;
    private DataView dataView;

   public Globals(MainActivity mainActivity) {
        this.mainActivity = mainActivity ;
        instanceNr++ ;
        new Thread ( new Tick () ).start();
    }

    public void setDataView(DataView dataView) {
        synchronized ( this ) {
            this.dataView = dataView ;
        }
    }

    class Tick implements Runnable {

        @Override
        public void run() {
            for ( int i = 0 ;; i++ ) {
                try {
                    Thread.sleep( 1000 ) ;
                    mainActivity.runOnUiThread( new SetText ( "TICK "+instanceNr+":"+i ) );
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    class SetText implements Runnable {

        private final String txt;

        SetText (String txt ) {
            this.txt = txt ;
        }
        @Override
        public void run() {
            synchronized ( Globals.this ) {
                if ( dataView != null )
                    dataView.setText( txt );
            }
        }
    }


}
