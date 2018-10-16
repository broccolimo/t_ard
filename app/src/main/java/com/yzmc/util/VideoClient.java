package com.yzmc.util;

import android.annotation.TargetApi;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.tutk.IOTC.AVAPIs;
import com.tutk.IOTC.IOTCAPIs;

import java.nio.ByteBuffer;


public class VideoClient {

    private static MediaCodec mediaCodec_video;
    private static MediaCodec mediaCodec_audio;
    private static AudioTrack player;
    private static boolean flag = true;


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void Start(String uid, MediaCodec mediaCodec_video, MediaCodec mediaCodec_audio, AudioTrack player){
        this.mediaCodec_video = mediaCodec_video;
        this.mediaCodec_audio = mediaCodec_audio;
        this.player = player;
        this.flag = true;
        //player.play();



        Log.d("tutk", "StreamClient start...");

        int ret = IOTCAPIs.IOTC_Initialize2(0);
        Log.d("tutk", "IOTC_Initialize() ret = " + ret);
        if (ret != IOTCAPIs.IOTC_ER_NoERROR) {
            Log.d("tutk", "IOTCAPIs_Device exit...!!");
            return;
        }

        AVAPIs.avInitialize(3);

        int sid = IOTCAPIs.IOTC_Get_SessionID();

        if (sid < 0){
            System.out.printf("IOTC_Get_SessionID error code [%d]\n", sid);
            return;
        }
        ret = IOTCAPIs.IOTC_Connect_ByUID_Parallel(uid, sid);
        //这句话明明是我自己加的 可是如果不写就-20009
        //不能光ret 还必须String.valueOf...
        Log.d("tutk", "ret = " + String.valueOf(ret));
        Log.d("tutk", "Step 2: call IOTC_Connect_ByUID_Parallel(" + uid + ")");

        int[] srvType = new int[1];
        //int avIndex = AVAPIs.avClientStart(sid, "admin", "admin", 20000, srvType, 0);
        int avIndex = AVAPIs.avClientStart(sid, "admin", "yzmc2018", 20000, srvType, 0);
        Log.d("tutk", "Step 2: call avClientStart(" + avIndex + ")");

        if (avIndex < 0) {
            Log.d("tutk", "avClientStart failed[" + avIndex + "]");
            return;
        }


        if (startIpcamStream(avIndex)) {
            Thread videoThread = new Thread(new VideoClient.VideoThread(avIndex),
                    "Video Thread");
            //Thread audioThread = new Thread(new VideoClient.AudioThread(avIndex),
                    //"Audio Thread");
            videoThread.start();
            //audioThread.start();

            try {
                videoThread.join();
            }
            catch (InterruptedException e) {
                return;
            }
            /*try {
                audioThread.join();
            }
            catch (InterruptedException e) {
                return;
            }*/
        }

        AVAPIs.avClientStop(avIndex);

        Log.d("tutk", "avClientStop OK");
        IOTCAPIs.IOTC_Session_Close(sid);
        Log.d("tutk", "IOTC_Session_Close OK");
        AVAPIs.avDeInitialize();
        IOTCAPIs.IOTC_DeInitialize();
        Log.d("tutk", "StreamClient exit...");
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean onFrame(byte[] buf, int offset, int length) {
        ByteBuffer[] inputBuffers = mediaCodec_video.getInputBuffers();
        int inputBufferIndex = mediaCodec_video.dequeueInputBuffer(-1);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            mediaCodec_video.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
        } else {
            return false;
        }
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec_video.dequeueOutputBuffer(bufferInfo, 100);
        while (outputBufferIndex >= 0) {
            mediaCodec_video.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mediaCodec_video.dequeueOutputBuffer(bufferInfo, 0);
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean onFrame2(byte[] buf, int offset, int length) {
        /*for(int i = 0; i < length - 1; i++){
            char a_val = (char) buf[i];
            int	t = 0;
            int	seg = 0;

            a_val ^= 0x55;

            t = (a_val & QUANT_MASK) << 4;
            seg = (a_val & SEG_MASK) >> SEG_SHIFT;

            switch (seg) {
                case 0:
                    t += 8;
                    break;
                case 1:
                    t += 0x108;
                    break;
                default:
                    t += 0x108;
                    t <<= seg - 1;
            }
            buf[i] = (byte)t;
        }


        player.write(buf, 0, length);*/
        /*short[] pcm = new short[length];
        for(int i = 0; i < length; i++){
            pcm[i] = g711ToPcm(buf[i]);
        }
        player.write(pcm, 0, length);
        return true;*/
        /*G711Coder.CoderResult res = new G711Coder.CoderResult();
        G711Coder mG711Coder = new G711Coder();
        mG711Coder.g711CoderInit();

        mG711Coder.g711uDecode(buf, length, res);*/


        /*short[] s = new short[length * 2];
        for(int i = 0; i < length; i++){
            char alaw = (char) buf[i];
            alaw ^= 0xD5;
            int sign = alaw & 0x80;
            int exponent = (alaw & 0x70) >> 4;
            int data = alaw & 0x0f;
            data <<= 4;
            data += 8;
            if(exponent != 0) data += 0x100;
            if(exponent > 1) data <<= (exponent - 1);
            s[i] = (short)(sign == 0 ? data : - data);
        }
        player.write(s, 0, length * 2);*/
        //player.write(buf, offset, length);
        Log.d("aaa", "11111");
        ByteBuffer[] codecInputBuffers = mediaCodec_audio.getInputBuffers();
        ByteBuffer[] codecOutputBuffers = mediaCodec_audio.getOutputBuffers();

        int inputBufIndex = mediaCodec_audio.dequeueInputBuffer(-1);
        if(inputBufIndex < 0) return false;
        if (inputBufIndex >= 0) {
            Log.d("aaa", "22222");
            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
            dstBuf.clear();
            dstBuf.put(buf, offset, length);
            mediaCodec_audio.queueInputBuffer(inputBufIndex, 0, length, 0, 0);



        }
        Log.d("aaa", "inputbuffers.length: " + codecInputBuffers.length);
        Log.d("aaa", "outputBuffers.length: " + codecOutputBuffers.length);
        Log.d("aaa", "33333");
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        int outputBufferIndex = mediaCodec_audio.dequeueOutputBuffer(info, 0);

        Log.d("aaa", "outputBufferIndex: " + outputBufferIndex);
        if(outputBufferIndex < 0) return false;
        while (outputBufferIndex >= 0) {
            /*Log.d("aaa", "44444");
            ByteBuffer outputBuffer = codecOutputBuffers[outputBufferIndex];
            byte[] outData = new byte[info.size];
            outputBuffer.get(outData);
            outputBuffer.clear();
            player.write(outData, 0, outData.length);
            mediaCodec_audio.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mediaCodec_audio.dequeueOutputBuffer(info, 0);
            Log.d("aaa", "55555");*/


            /*ByteBuffer outputBuffer = mediaCodec_audio.getOutputBuffer(outputBufferIndex);
            MediaFormat format = mediaCodec_audio.getOutputFormat();
            ShortBuffer samples = outputBuffer.order(ByteOrder.nativeOrder()).asShortBuffer();
            System.out.println("@@@@@@@@@@@@@" + samples.get(0));
            int numChannels = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
            System.out.println("#############" + numChannels);
            short[] res = new short[samples.remaining() / numChannels];
            for (int i = 0; i < res.length; ++i) {
                res[i] = samples.get(i * numChannels + 2);
            }
            player.write(res, 0, res.length);*/
        }
        return true;
    }


    public static boolean startIpcamStream(int avIndex) {
        AVAPIs av = new AVAPIs();
        int ret = av.avSendIOCtrl(avIndex, AVAPIs.IOTYPE_INNER_SND_DATA_DELAY,
                new byte[2], 2);
        if (ret < 0) {
            System.out.printf("start_ipcam_stream failed[%d]\n", ret);
            return false;
        }

        // This IOTYPE constant and its corrsponsing data structure is defined in
        // Sample/Linux/Sample_AVAPIs/AVIOCTRLDEFs.h

        int IOTYPE_USER_IPCAM_START = 0x1FF;
        ret = av.avSendIOCtrl(avIndex, IOTYPE_USER_IPCAM_START,
                new byte[8], 8);
        if (ret < 0) {
            System.out.printf("start_ipcam_stream failed[%d]\n", ret);
            return false;
        }

        int IOTYPE_USER_IPCAM_AUDIOSTART = 0x300;
        ret = av.avSendIOCtrl(avIndex, IOTYPE_USER_IPCAM_AUDIOSTART,
                new byte[8], 8);
        if (ret < 0) {
            System.out.printf("start_ipcam_stream failed[%d]\n", ret);
            return false;
        }

        return true;
    }

    public static class VideoThread implements Runnable {
        static final int VIDEO_BUF_SIZE = 100000;
        static final int FRAME_INFO_SIZE = 16;

        private int avIndex;
        public VideoThread(int avIndex) {
            this.avIndex = avIndex;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            System.out.printf("[%s] Start\n",
                    Thread.currentThread().getName());

            AVAPIs av = new AVAPIs();
            byte[] frameInfo = new byte[FRAME_INFO_SIZE];
            byte[] videoBuffer = new byte[VIDEO_BUF_SIZE];
            int[] outBufSize = new int[1];
            int[] outFrameSize = new int[1];
            int[] outFrmInfoBufSize = new int [1];
            while (true) {
                int[] frameNumber = new int[1];
                int ret = av.avRecvFrameData2(avIndex, videoBuffer,
                        VIDEO_BUF_SIZE, outBufSize, outFrameSize,
                        frameInfo, FRAME_INFO_SIZE,
                        outFrmInfoBufSize, frameNumber);
                if (ret == AVAPIs.AV_ER_DATA_NOREADY) {
                    try {
                        Thread.sleep(30);
                        continue;
                    }
                    catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }
                else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                    System.out.printf("[%s] Lost video frame number[%d]\n",
                            Thread.currentThread().getName(), frameNumber[0]);
                    continue;
                }
                else if (ret == AVAPIs.AV_ER_INCOMPLETE_FRAME) {
                    System.out.printf("[%s] Incomplete video frame number[%d]\n",
                            Thread.currentThread().getName(), frameNumber[0]);
                    continue;
                }
                else if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
                    System.out.printf("[%s] AV_ER_SESSION_CLOSE_BY_REMOTE\n",
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
                    System.out.printf("[%s] AV_ER_REMOTE_TIMEOUT_DISCONNECT\n",
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_INVALID_SID) {
                    System.out.printf("[%s] Session cant be used anymore\n",
                            Thread.currentThread().getName());
                    break;
                }

                // Now the data is ready in videoBuffer[0 ... ret - 1]
                // Do something here
                try{
                    onFrame(videoBuffer, 0, videoBuffer.length);
                }
                catch (Exception e){
                    flag = false;
                    break;
                }

            }

            System.out.printf("[%s] Exit\n",
                    Thread.currentThread().getName());
        }
    }

    public static class AudioThread implements Runnable {
        static final int AUDIO_BUF_SIZE = 1024;
        static final int FRAME_INFO_SIZE = 16;

        private int avIndex;

        public AudioThread(int avIndex) {
            this.avIndex = avIndex;
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void run() {
            System.out.printf("[%s] Start\n",
                    Thread.currentThread().getName());

            AVAPIs av = new AVAPIs();
            byte[] frameInfo = new byte[FRAME_INFO_SIZE];
            byte[] audioBuffer = new byte[AUDIO_BUF_SIZE];


            while (true) {
                int ret = av.avCheckAudioBuf(avIndex);

                if (ret < 0) {
                    // Same error codes as below
                    System.out.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          printf("[%s] avCheckAudioBuf() failed: %d\n",
                            Thread.currentThread().getName(), ret);
                    break;
                }
                else if (ret < 3) {
                    try {
                        Thread.sleep(120);
                        continue;
                    }
                    catch (InterruptedException e) {
                        System.out.println(e.getMessage());
                        break;
                    }
                }

                int[] frameNumber = new int[1];
                ret = av.avRecvAudioData(avIndex, audioBuffer,
                        AUDIO_BUF_SIZE, frameInfo, FRAME_INFO_SIZE,
                        frameNumber);

                if (ret == AVAPIs.AV_ER_SESSION_CLOSE_BY_REMOTE) {
                    System.out.printf("[%s] AV_ER_SESSION_CLOSE_BY_REMOTE\n",
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_REMOTE_TIMEOUT_DISCONNECT) {
                    System.out.printf("[%s] AV_ER_REMOTE_TIMEOUT_DISCONNECT\n",
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_INVALID_SID) {
                    System.out.printf("[%s] Session cant be used anymore\n",
                            Thread.currentThread().getName());
                    break;
                }
                else if (ret == AVAPIs.AV_ER_LOSED_THIS_FRAME) {
                    //System.out.printf("[%s] Audio frame losed\n",
                    //        Thread.currentThread().getName());
                    continue;
                }
                // Now the data is ready in audioBuffer[0 ... ret - 1]
                // Do something here

               try{
                    //onFrame2(audioBuffer, 0, audioBuffer.length);
                    if(!flag){
                        /*player.stop();
                        player.release();*/
                        break;
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    break;
                }
            }
            System.out.printf("[%s] Exit\n",
                    Thread.currentThread().getName());
        }
    }

}
