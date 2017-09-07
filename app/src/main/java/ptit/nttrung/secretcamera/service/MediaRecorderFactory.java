package ptit.nttrung.secretcamera.service;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import ptit.nttrung.secretcamera.helper.CameraHelper;


/**
 * Created by TrungNguyen on 5/21/2017.
 */

public class MediaRecorderFactory {

    private final static String TAG = MediaRecorderFactory.class.getName();

    public static CamcorderProfile setCamcorderProfile(String quality) {
        int videoQuality = 0;
        CamcorderProfile profile = null;
        try {
            videoQuality = Integer.parseInt(quality);
            switch (videoQuality) {
                case 0:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                    break;
                case 1:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                    break;
                case 2:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
                    break;
                default:
                    profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                    break;
            }
        } catch (Exception e) {
            profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        }
        return profile;
    }

    public static Camera setVideoCamera(String useCamera) {
        int videoCamera = 0;
        Camera camera = null;
        try {
            videoCamera = Integer.parseInt(useCamera);
            switch (videoCamera) {
                case 0:
                    camera = CameraHelper.getDefaultBackFacingCameraInstance();
                    break;
                case 1:
                    camera = CameraHelper.getDefaultFrontFacingCameraInstance();
                    break;
                default:
                    camera = CameraHelper.getDefaultCameraInstance();
                    break;
            }
        } catch (Exception e) {
            camera = CameraHelper.getDefaultCameraInstance();
        }
        if (camera == null) camera = CameraHelper.getDefaultCameraInstance();
        return camera;
    }

    public static MediaRecorder setAudioSource(MediaRecorder mediaRecorder, String audioSource) {
        int audio = Integer.parseInt(audioSource);
        switch (audio) {
            case 0:
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                break;
            case 1:
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                break;
            case 2:
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                break;
            default:
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
                break;
        }
        return mediaRecorder;
    }

    public static MediaRecorder setSource(MediaRecorder mediaRecorder, boolean isSlient, String audioSource, CamcorderProfile profile) {
        if (!isSlient) {
            MediaRecorder.getAudioSourceMax();
            mediaRecorder = MediaRecorderFactory.setAudioSource(mediaRecorder, audioSource);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
            mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
            mediaRecorder.setAudioEncodingBitRate(profile.audioBitRate);
            mediaRecorder.setAudioChannels(profile.audioChannels);
            mediaRecorder.setAudioSamplingRate(profile.audioSampleRate);
            mediaRecorder.setVideoEncoder(profile.videoCodec);
            mediaRecorder.setAudioEncoder(profile.audioCodec);
        } else {
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setVideoFrameRate(profile.videoFrameRate);
            mediaRecorder.setVideoSize(profile.videoFrameWidth, profile.videoFrameHeight);
            mediaRecorder.setVideoEncodingBitRate(profile.videoBitRate);
            mediaRecorder.setVideoEncoder(profile.videoCodec);
        }

        return mediaRecorder;
    }

    public static MediaRecorder setOrientation(MediaRecorder mediaRecorder, String orientation, String useCamera) {
        int oriention = Integer.parseInt(orientation);

        int videoCamera = Integer.parseInt(useCamera);
        if (videoCamera == 1) {
            switch (oriention) {
                case 0:
                    mediaRecorder.setOrientationHint(90 + 180);
                    break;
                case 1:
                    mediaRecorder.setOrientationHint(90 + 180);
                    break;
                case 2:
                    mediaRecorder.setOrientationHint(180 + 180);
                    break;
                case 3:
                    mediaRecorder.setOrientationHint(270 - 180);
                    break;
                case 4:
                    mediaRecorder.setOrientationHint(360 - 180);
                    break;
            }
        } else {
            switch (oriention) {
                case 0:
                    mediaRecorder.setOrientationHint(90);
                    break;
                case 1:
                    mediaRecorder.setOrientationHint(90);
                    break;
                case 2:
                    mediaRecorder.setOrientationHint(180);
                    break;
                case 3:
                    mediaRecorder.setOrientationHint(270);
                    break;
                case 4:
                    mediaRecorder.setOrientationHint(360);
                    break;
            }
        }
        return mediaRecorder;
    }
}
