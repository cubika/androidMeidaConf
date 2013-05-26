package com.rongdian;

import java.util.List;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.SurfaceHolder.Callback;
import android.view.WindowManager;

public abstract class RtpAvTermAndroidActivity extends Activity implements
		Camera.PreviewCallback, Callback, RtpAvTermListener {
	private static final int frequency = 8000;
	private static final int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
	private static final int audioRecordMinBufSize = AudioRecord
			.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
	private static final int audioTraceMinBufSize = AudioRecord
			.getMinBufferSize(frequency, channelConfiguration, audioEncoding);
	private static final int playBufSize = audioTraceMinBufSize / 4;
	// 音频的采集和播放
	private boolean isRecording = false;
	private AudioRecord audioRecord = null;
	private AudioTrack audioTrack = null;

	// 视频的采集和播放
	private Camera mCamera = null;
	// 预览状态
	private boolean mPreviewRunning = false;
	SurfaceView mSurfaceView = null;
	private int mMakeRotationClockwise = 0;
	// 视频参数
	private long mVideoWidth = 0;
	private long mVideoHeight = 0;
	private boolean isVideoOutput = false;

	private long term = 0;

	//
	// //-------------------------------------------------------------------------
	//
	protected abstract int getLayoutId();

	protected abstract SurfaceView getLocalPreview();

	protected abstract GL2JNIView getRomotePreview();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(getLayoutId());

		mSurfaceView = getLocalPreview();
		SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
		surfaceHolder.addCallback(this);
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getRomotePreview().onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getRomotePreview().onResume();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}

	//
	// //-----对外公开API--------------------------------------------------------------------------------
	//

	public synchronized boolean ravtCreateTerm() {
		if (this.term != 0) {
			return (false);
		}

		this.term = RtpAvTerm.ravtCreateTerm(this);

		return (this.term != 0);
	}

	public void ravtDeleteTerm() {
		long term = 0;

		synchronized (this) {
			getRomotePreview().renderer.setOpenGLESDisplay(0);
			term = this.term;
			this.term = 0;
		}

		if (term != 0) {
			RtpAvTerm.ravtDeleteTerm(term);
		}
	}

	public synchronized boolean ravtOpenVideoSession(byte[] localIp,
			int localPort) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtOpenVideoSession(this.term, localIp, localPort);
	}

	public synchronized void ravtCloseVideoSession() {
		if (this.term == 0) {
			return;
		}

		RtpAvTerm.ravtCloseVideoSession(this.term);
	}

	public synchronized boolean ravtGetVideoSessionLocalAddr(byte[] localIp_64,
			int[] localPort_1) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtGetVideoSessionLocalAddr(this.term, localIp_64,
				localPort_1);
	}

	public synchronized boolean ravtSetVideoSessionRemoteAddr(byte[] remoteIp,
			int remotePort) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtSetVideoSessionRemoteAddr(this.term, remoteIp,
				remotePort);
	}

	public synchronized boolean ravtSetVideoSessionOutputPayloadType(
			byte payloadType) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtSetVideoSessionOutputPayloadType(this.term,
				payloadType);
	}

	public synchronized boolean ravtOpenLocalVideoPreview(long videoWidth,
			long videoHeight) {

		if (videoWidth < videoHeight) {
			videoWidth ^= videoHeight;
			videoHeight ^= videoWidth;
			videoWidth ^= videoHeight;
		}

		if (!RtpAvTerm.ravtOpenLocalVideoPreview(this.term, videoWidth,
				videoHeight)) {
			return (false);
		}

		SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
		mVideoWidth = videoWidth;
		mVideoHeight = videoHeight;

		mCamera = Camera.open(getFrontCameraId());

		if (mPreviewRunning) {
			mCamera.stopPreview();
		}

		Camera.Parameters p = mCamera.getParameters();
		// 设置预览大小
		Size psize = matchCameraSuportSize(videoWidth, videoHeight, p);
		p.setPreviewSize(psize.width, psize.height);
		Log.i("wzl" + "initCamera", "previewSize,width: " + psize.width
				+ " height" + psize.height);

		// 设置采集帧率
		p.setPreviewFrameRate(findCameraSuportMaxFrameRate(p));
		Log.i("wzl" + "initCamera",
				"previewFrameRates: "
						+ String.valueOf(findCameraSuportMaxFrameRate(p)));

		// 调整预览的方向正常
		if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
			p.set("orientation", "portrait");
			mCamera.setDisplayOrientation(90);// 针对android2.2和之上的版本
			p.setRotation(90);// 去掉android2.0和之前的版本
			mMakeRotationClockwise = 90;
		} else {
			p.set("orientation", "landscape");
			mCamera.setDisplayOrientation(0);
			p.setRotation(0);
			mMakeRotationClockwise = 0;
		}

		mCamera.setParameters(p);

		mCamera.setPreviewCallback(this);
		try {
			// 设置预览的控件，图像会绘画在这个控件上。不需要人工去画
			mCamera.setPreviewDisplay(surfaceHolder);
		} catch (Exception ex) {
			RtpAvTerm.ravtCloseLocalVideoPreview(this.term);
			return (false);
		}

		// 开始预览
		mCamera.startPreview();
		mPreviewRunning = true;

		return (true);
	}

	// 返回前置摄像头id如果没有返回后置摄像头
	private int getFrontCameraId() {
		int cameraId = 0;
		int numberOfCameras = Camera.getNumberOfCameras();
		CameraInfo cameraInfo = new CameraInfo();
		for (int i = 0; i < numberOfCameras; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
				cameraId = i;
				break;
			} else if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
				cameraId = i;
			}
		}
		return (cameraId);
	}

	private Size matchCameraSuportSize(long videoWidth, long videoHeight,
			Camera.Parameters p) {
		List<Size> previewSizes = p.getSupportedPreviewSizes();
		Size psize;
		int k = 0;
		for (int i = 0; i < previewSizes.size(); i++) {
			psize = previewSizes.get(i);
			Log.i("wzl" + "initCamera", "previewSize,width: " + psize.width
					+ " height" + psize.height);
			if ((psize.width == videoWidth) && (psize.height == videoHeight)) {
				psize.width = (int) videoWidth;
				psize.height = (int) videoHeight;
				return (psize);
			}
		}
		for (int i = 0; i < previewSizes.size(); i++) {
			psize = previewSizes.get(i);
			if (Math.pow((previewSizes.get(k).width
					* previewSizes.get(k).height - videoWidth * videoHeight), 2) > Math
					.pow((psize.width * psize.height - videoWidth * videoHeight),
							2)) {
				k = i;
			}
		}
		psize = previewSizes.get(k);
		return (psize);
	}

	private int findCameraSuportMaxFrameRate(Camera.Parameters p) {
		int k = 0;
		List<Integer> previewFrameRates = p.getSupportedPreviewFrameRates();
		for (int i = 0; i < previewFrameRates.size(); i++) {
			previewFrameRates.get(i);
			if (previewFrameRates.get(k) < previewFrameRates.get(i)) {
				k = i;
			}
		}
		return previewFrameRates.get(k).intValue();
	}

	public synchronized void ravtCloseLocalVideoPreview() {
		if (this.term == 0) {
			return;
		}

		if (mPreviewRunning) {
			mPreviewRunning = false;
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
		}

		RtpAvTerm.ravtCloseLocalVideoPreview(this.term);
	}

	public synchronized boolean ravtOpenLocalVideoOutput(short videoType,
			long videoWidth, long videoHeight, long videoBitRate,
			long videoFrameRate, long videoKeyFrameInterval,
			long videoPacketSize) {
		if (!RtpAvTerm.ravtOpenLocalVideoOutput(this.term, videoType,
				videoWidth, videoHeight, videoBitRate, videoFrameRate,
				videoKeyFrameInterval, videoPacketSize)) {
			return (false);
		}

		this.isVideoOutput = true;

		return (true);
	}

	public synchronized void ravtCloseLocalVideoOutput() {
		if (this.term == 0) {
			return;
		}

		this.isVideoOutput = false;
		RtpAvTerm.ravtCloseLocalVideoOutput(this.term);
	}

	public synchronized boolean ravtOpenRemoteVideoPreview(short videoType) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtOpenRemoteVideoPreview(this.term, videoType);
	}

	public synchronized void ravtCloseRemoteVideoPreview() {
		if (this.term == 0) {
			return;
		}

		RtpAvTerm.ravtCloseRemoteVideoPreview(this.term);
	}

	public synchronized boolean ravtOpenAudioSession(byte[] localIp,
			int localPort) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtOpenAudioSession(this.term, localIp, localPort);
	}

	public synchronized boolean ravtGetAudioSessionLocalAddr(byte[] localIp_64,
			int[] localPort_1) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtGetAudioSessionLocalAddr(this.term, localIp_64,
				localPort_1);
	}

	public synchronized boolean ravtSetAudioSessionRemoteAddr(byte[] remoteIp,
			int remotePort) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtSetAudioSessionRemoteAddr(this.term, remoteIp,
				remotePort);
	}

	public synchronized boolean ravtSetAudioSessionOutputPayloadType(
			byte payloadType) {
		if (this.term == 0) {
			return (false);
		}

		return RtpAvTerm.ravtSetAudioSessionOutputPayloadType(this.term,
				payloadType);
	}

	public synchronized void ravtCloseAudioSession() {
		if (this.term == 0) {
			return;
		}

		RtpAvTerm.ravtCloseAudioSession(this.term);
	}

	public synchronized boolean ravtOpenLocalAudio(short audioType) {
		if (!RtpAvTerm.ravtOpenLocalAudio(this.term, audioType, 8000)) {
			return (false);
		}

		if (!isRecording) {
			isRecording = true;
			new Thread(new Runnable() {
				@Override
				public void run() {
					audioCapture();
				}
			}).start();
		}

		return (true);
	}

	public synchronized void ravtCloseLocalAudio() {
		if (this.term == 0) {
			return;
		}

		isRecording = false;
		RtpAvTerm.ravtCloseLocalAudio(this.term);
	}

	// public Object remoteAudio_lock = new Object();

	public synchronized boolean ravtOpenRemoteAudio(short audioType) {
		if (this.term == 0) {
			return (false);
		}

		// 创建pcm音频播放器并开启
		if (!RtpAvTerm.ravtOpenRemoteAudio(this.term, audioType, 8000)) {
			return (false);
		}

		if (audioTrack == null) {
			audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
					frequency, audioEncoding, audioEncoding,
					audioTraceMinBufSize * 3, AudioTrack.MODE_STREAM);
			audioTrack.play();
		}

		return (true);
	}

	public synchronized void ravtCloseRemoteAudio() {
		if (this.term == 0) {
			return;
		}

		if (audioTrack != null) {
			audioTrack.flush();
			audioTrack.stop();
			audioTrack.release();
			audioTrack = null;
		}

		RtpAvTerm.ravtCloseRemoteAudio(this.term);
	}

	//
	// //-----RtpAvTermListener接口中的方法供底层回调--------------------------------------------------------------------------------
	//

	@Override
	public synchronized void onRecvAudio(long term, byte[] pcmData, int size) {
		if (this.term == 0) {
			return;
		}

		if (audioTrack != null) {
			if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
				audioTrack.write(pcmData, 0, size);
			}
		}
	}

	@Override
	public synchronized void onRecvVideo(long term) {
		if (this.term == 0) {
			return;
		}

		getRomotePreview().renderer.setOpenGLESDisplay(this.term);
		// 刷新界面
		getRomotePreview().requestRender();
	}

	@Override
	public synchronized void onRequestKeyFrame(long term) {
		// TODO Auto-generated method stub
	}

	//
	// //-----视音频数据采集--------------------------------------------------------------------------------
	//

	// camera回调。此会自动采集一帧yuv420sp数据
	public synchronized void onPreviewFrame(byte[] data, Camera camera) {
		if (this.term == 0) {
			return;
		}

		if (isVideoOutput) {
			RtpAvTerm.ravtPutLocalVideoOutput(this.term, data, mVideoWidth,
					mVideoHeight, RtpAvTerm.RAVT_COLOR_YUV420SP_VU,
					mMakeRotationClockwise);
		}
	}

	private void audioCapture() {
		if (audioRecord != null) {
			return;
		}

		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frequency,
				channelConfiguration, audioEncoding, audioRecordMinBufSize);
		audioRecord.startRecording();

		byte[] buffer = new byte[playBufSize];

		while (true) {
			synchronized (this) {
				if (!isRecording) {
					break;
				}
			}

			audioRecord.read(buffer, 0, playBufSize);

			synchronized (this) {
				if (this.term != 0) {
					RtpAvTerm.ravtPutLocalAudio(this.term, buffer);
				}
			}
		}

		audioRecord.stop();
		audioRecord.release();
		audioRecord = null;
	}
}