package com.manning.aip.media;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import com.manning.aip.media.AudioBrowserActivity.Song;

public class SlideshowActivity extends Activity {

	private ImageView leftSlide;
	private ImageView rightSlide;
	private Handler handler = new Handler();
	private static final int TIME_PER_SLIDE = 3*1000;
	private Song song;
	private MediaPlayer player;
	private MediaController videoPlayer;
	private VideoView video;
	private boolean playingSlides = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.slideshow);
		leftSlide = (ImageView) findViewById(R.id.slide0);
		rightSlide = (ImageView) findViewById(R.id.slide1);
		song = getIntent().getParcelableExtra("selectedSong");
		player = MediaPlayer.create(this, song.uri);
		player.setLooping(false);
		player.setOnCompletionListener(new OnCompletionListener(){
			@Override
			public void onCompletion(MediaPlayer mp) {
				FrameLayout frame = (FrameLayout) findViewById(R.id.frame);
				frame.removeAllViews();
				playingSlides = false;
//				video = (VideoView) getLayoutInflater().inflate(
//							R.layout.video_player, frame, false);
				video = new VideoView(SlideshowActivity.this);
				video.setLayoutParams(new LayoutParams(
								LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				frame.addView(video);
				video.setVideoURI((Uri) getIntent().getExtras().get("videoUri")); 
				videoPlayer = new MediaController(SlideshowActivity.this);
				videoPlayer.setMediaPlayer(video);
				video.setMediaController(videoPlayer);
				video.requestFocus();
				video.start();
			}
		});
	}
	
	@Override
	public void onPause(){
		super.onPause();
		if (player != null && player.isPlaying()){
			player.pause();
		}
		if (video != null && video.isPlaying()){
			video.pause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (player != null && player.isPlaying()){
			player.stop();
		}
		if (video != null && video.isPlaying()){
			video.stopPlayback();
		}
		player.release();
	}

	@Override
	public void onResume() {
		super.onResume();
		handler.postDelayed(new DissolveTransition(), 100);
		player.start();
	}

	private class DissolveTransition implements Runnable{

		private ArrayList<String> images;
		private int count = 0;
		private Bitmap currentImage = null;
		int current = -1;
		private Bitmap nextImage = null;
		private Random rnd = new Random(System.currentTimeMillis());

		public DissolveTransition() {
			images = getIntent().getStringArrayListExtra("imageFileNames");
			currentImage = getNextImage();			
			leftSlide.setImageBitmap(currentImage);
			nextImage = getNextImage();
			rightSlide.setImageBitmap(nextImage);
			count = 1;
		}

		@Override
		public void run() {
			nextSlide();
		}
		
		private Bitmap getImage(int index){
			return BitmapFactory.decodeFile(images.get(index));
		}

		private void nextSlide() {
			AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
			if ((count % 2) == 0) {
				animation = new AlphaAnimation(1.0f, 0.0f);
			}
			animation.setStartOffset(TIME_PER_SLIDE);
			animation.setDuration(TIME_PER_SLIDE);
			animation.setFillAfter(true);
			animation.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					if (playingSlides){
						nextImage = getNextImage();
						ImageView backgroundImage = 
							(count % 2 == 0) ? rightSlide : leftSlide;
						backgroundImage.setImageBitmap(nextImage);
						count++;
						nextSlide();
					}
				}
			});
			rightSlide.startAnimation(animation);
			currentImage = nextImage;
		}
		
		public Bitmap getNextImage(){
			int index = rnd.nextInt(images.size());
			if (current < 0) current = index;
			while (index == current){
				index = rnd.nextInt(images.size());
			}
			current = index;
			return getImage(index);
		}
	}
}