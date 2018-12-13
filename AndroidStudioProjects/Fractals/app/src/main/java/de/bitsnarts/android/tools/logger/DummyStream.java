package de.bitsnarts.android.tools.logger;

import java.io.IOException;
import java.io.OutputStream;

class DummyStream extends OutputStream {

    @Override
    public void write(int b) throws IOException {
    }
}
