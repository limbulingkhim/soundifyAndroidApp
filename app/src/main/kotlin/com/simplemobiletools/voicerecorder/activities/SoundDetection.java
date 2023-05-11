
package com.simplemobiletools.voicerecorder.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioRecord;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import com.simplemobiletools.voicerecorder.R;
import com.simplemobiletools.voicerecorder.helpers.MLAudioHelperActivity;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SoundDetection extends MLAudioHelperActivity {

    String modelPath = "data_model.tflite";
    float probabilityThreshold = 0.3f;
    AudioClassifier classifier;
    private TensorAudio tensor;
    private AudioRecord record;
    private TimerTask timerTask;
    private ImageView imageView;


    public void onStartRecording(View view) {
        super.onStartRecording(view);

        // Loading the model from the assets folder
        try {
            classifier = AudioClassifier.createFromFile(this, modelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Creating an audio recorder
        tensor = classifier.createInputTensorAudio();

        // showing the audio recorder specification
        TensorAudio.TensorAudioFormat format = classifier.getRequiredTensorAudioFormat();
        String specs = "Number of channels: " + format.getChannels() + "\n"
            + "Sample Rate: " + format.getSampleRate();
        specsTextView.setText(specs);

        // Creating and start recording
        record = classifier.createAudioRecord();
        record.startRecording();

        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.d(SoundDetection.class.getSimpleName(), "timer task triggered");
                // Classifying audio data
                // val numberOfSamples = tensor.load(record)
                // val output = classifier.classify(tensor)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int numberOfSamples = tensor.load(record);
                }
                List<Classifications> output = classifier.classify(tensor);
                Log.i("output",output.toString());

                // Filtering out classifications with low probability
                List<Category> finalOutput = new ArrayList<>();
//                for (Classifications classifications : output) {
                for (Category category : output.get(0).getCategories()) {

                    if ( category.getScore() > probabilityThreshold) {
                        finalOutput.add(category);

                    }
                }
//                }

                if (finalOutput.isEmpty()) {
                    return;
                }

//                finalOutput = new ArrayList<>();
//                for (Category category : output.get(1).getCategories()) {
//                    if (category.getScore() > probabilityThreshold) {
//                        finalOutput.add(category);
//                    }
//                }

                // Sorting the results
                Collections.sort(finalOutput, (o1, o2) -> (int) (o1.getScore() - o2.getScore()));

                // Creating a multiline string with the filtered results
                StringBuilder outputStr = new StringBuilder();
                for (Category category : finalOutput) {
                    outputStr
                        .append(category.getLabel().substring(2))
//                            .append(": ").append(category.getScore())
//                            .append(", ")
                        .append(category.getDisplayName());
                }

                // Updating the UI
                List<Category> finalOutput1 = finalOutput;
                runOnUiThread(() -> {
                    if (finalOutput1.isEmpty()) {
                        outputTextView.setText("Could not identify the sound");
                    } else {
                        //outputTextView.setText(outputStr.toString());
                        String name = outputStr.toString();
                        imageView = findViewById(R.id.object_image);
                        if(name.equals("Background Noise")){
                            imageView.setImageResource(R.drawable.noise);
                            outputTextView.setText("Background Noise");
                        }else if(name.equals("Cat")){
                            imageView.setImageResource(R.drawable.cat1);
                            outputTextView.setText("Cat's meow");
                            addNotification("Cat");

                        }else if(name.equals("Dog")){
                            imageView.setImageResource(R.drawable.dog);
                            outputTextView.setText("Dog's bark");

                            addNotification("Dog");
                        }else if(name.equals("Door Bell")){
                            imageView.setImageResource(R.drawable.horn);
                            outputTextView.setText("Door Bell");
                        }else if(name.equals("Door Knock")){
                            imageView.setImageResource(R.drawable.door);
                            outputTextView.setText("Door knock");
                        } else if(name.equals("Baby Crying")){
                            imageView.setImageResource(R.drawable.baby_crying);
                            outputTextView.setText("Baby Crying");
                            addNotification("Baby's cry");
                        }else{
                            imageView.setImageResource(R.drawable.noise);
                        }


                    }
                });
            }
        };

        new Timer().scheduleAtFixedRate(timerTask, 1, 500);
    }

    public void onStopRecording(View view) {
        super.onStopRecording(view);

        timerTask.cancel();
        record.stop();
    }
    private void addNotification(String content_text) {
//        NotificationCompat.Builder builder =
//            new NotificationCompat.Builder(this)
//                .setSmallIcon(R.drawable.horn)
//                .setContentTitle("Notifications Example")
//                .setContentText("This is a test notification");

//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, String.valueOf(14))
//            .setSmallIcon(R.drawable.horn)
//            .setContentTitle("Sound Detection")
//            .setContentText("New sound is detecting")
//            .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//        Intent notificationIntent = new Intent(this, MainActivity.class);
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
//            PendingIntent.FLAG_UPDATE_CURRENT);
//        builder.setContentIntent(contentIntent);
//
//        // Add as notification
//        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        manager.notify(0, builder.build());

        //creating the notification channel
        int NOTIFICATION_ID = 234;
        NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String CHANNEL_ID = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            CHANNEL_ID = "my_channel_01";
            CharSequence name = "my_channel";
            String Description = "This is my channel";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            mChannel.setDescription(Description);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            mChannel.setShowBadge(false);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Sound Detection")
            .setContentText(content_text + " is detecting");

        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
