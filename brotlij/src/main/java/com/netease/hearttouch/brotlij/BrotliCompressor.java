package com.netease.hearttouch.brotlij;

import java.io.ByteArrayOutputStream;

/**
 * Created by hanpfei0306 on 17-3-31.
 */

public class BrotliCompressor {
    private static final int DATA_IN_BUFFER_MAX_SIZE = 32 * 1024;
    private static final int DATA_OUT_BUFFER_MAX_SIZE = 64 * 1024;

    private static final int DATA_IN_BUFFER_MIN_SIZE = 8 * 1024;
    private static final int DATA_OUT_BUFFER_MIN_SIZE = 16 * 1024;

    private final long mEncoderInstance;

    private byte[] mDataInBuffer;
    private byte[] mDataOutBuffer;

    private ByteArrayOutputStream mOutputByteArrayOS;

    public BrotliCompressor() {
        int mLgblock = Brotli.DEFAULT_LGBLOCK;
        int mLgwin = Brotli.DEFAULT_LGWIN;
        int mQuality = Brotli.DEFAULT_QUALITY;
        Brotli.Mode mMode = Brotli.DEFAULT_MODE;
        mEncoderInstance = nativeCreateBrotliCompressorInstance(mMode.mode, mQuality, mLgwin, mLgblock);
        mOutputByteArrayOS = new ByteArrayOutputStream();
    }

    private void checkState() {
        if (mOutputByteArrayOS == null) {
            throw new IllegalStateException("The encoder has been finished.");
        }
    }

    public void compressData(byte[] data, int startPos, int length, boolean isEof) {
        checkState();

        int remainingStart = startPos;
        int endPos = startPos + length;

        if (mDataInBuffer == null) {
            if (length > DATA_IN_BUFFER_MAX_SIZE) {
                mDataInBuffer = new byte[DATA_IN_BUFFER_MAX_SIZE];
                mDataOutBuffer = new byte[DATA_OUT_BUFFER_MAX_SIZE];
            } else if (length > DATA_IN_BUFFER_MIN_SIZE) {
                mDataInBuffer = new byte[length];
                mDataOutBuffer = new byte[length * 2];
            } else {
                mDataInBuffer = new byte[DATA_IN_BUFFER_MIN_SIZE];
                mDataOutBuffer = new byte[DATA_OUT_BUFFER_MIN_SIZE];
            }
        }
        byte[] dataInBuffer = mDataInBuffer;
        byte[] dataOutBuffer = mDataOutBuffer;
        while (remainingStart + dataInBuffer.length < endPos) {
            System.arraycopy(data, remainingStart, dataInBuffer, 0, dataInBuffer.length);
            int commpressedDataSize = nativeCompress(mEncoderInstance, dataInBuffer, 0,
                    dataInBuffer.length, dataOutBuffer, false);
            mOutputByteArrayOS.write(dataOutBuffer, 0, commpressedDataSize);

            remainingStart += dataInBuffer.length;
        }

        System.arraycopy(data, remainingStart, dataInBuffer, 0, endPos - remainingStart);
        int commpressedDataSize = nativeCompress(mEncoderInstance, dataInBuffer, 0,
                endPos - remainingStart, dataOutBuffer, isEof);
        mOutputByteArrayOS.write(dataOutBuffer, 0, commpressedDataSize);
    }

    public byte[] toByteArray() {
        checkState();

        return mOutputByteArrayOS.toByteArray();
    }

    public boolean compressFile(String inputFilePath, String outputFilePath) {
        return nativeCompressFile(mEncoderInstance, inputFilePath, outputFilePath);
    }

    public void finish() {
        checkState();

        nativeDestroyBrotliCompressorInstance(mEncoderInstance);
        mOutputByteArrayOS = null;
    }

    private native boolean nativeCompressFile(long encoderInstance, String inputFilePath, String outputFilePath);

    private native long nativeCreateBrotliCompressorInstance(int mode, int quality, int lgwin, int lgblock);
    private native int nativeCompress(long encoderInstance, byte[] data, int startPos, int length, byte[] compressedData, boolean isEof);
    private native void nativeDestroyBrotliCompressorInstance(long encoderInstance);
}
