package com.dodemy.cardviewdashboard.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import com.dodemy.cardviewdashboard.classes.PaintView;
import com.dodemy.cardviewdashboard.R;

import java.util.Locale;

public class SpeakFragment extends Fragment {
    private PaintView paintView;
    private FloatingActionButton speakButton;
    private FloatingActionButton resetButton;
    public TextToSpeech mTextToSpeech;
    private String resultText = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_speak, container, false);

        paintView = view.findViewById(R.id.paintview);
        final DisplayMetrics metrics = new DisplayMetrics();
        assert getActivity() != null;
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        paintView.init(metrics);

        speakButton = view.findViewById(R.id.speak_button);
        resetButton = view.findViewById(R.id.reset_button);

        mTextToSpeech = new TextToSpeech(getActivity().getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS)
                    mTextToSpeech.setLanguage(Locale.US);
                else
                    mTextToSpeech = null;
                Log.e("SpeakFragment", "Failed to initialize the TextToSpeech engine");
            }
        });

        //reset button to clear dashboard
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.clear();
            }
        });

        //button to start text recognition and text to speech
        speakButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.setDrawingCacheEnabled(true); //saving image
                Bitmap bitmap = paintView.getDrawingCache();
                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                //create detector
                FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance().getOnDeviceTextRecognizer();

                Task<FirebaseVisionText> result =
                        detector.processImage(image)
                                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                                    @Override
                                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                        extractText(firebaseVisionText);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                    }
                                });
            }
        });

        return view;
    }

    /**
     * text recognition for dashboard screen (hand-writing)
     *
     * @param result
     */
    public void extractText(FirebaseVisionText result) {
        resultText = result.getText();
        for (FirebaseVisionText.TextBlock block : result.getTextBlocks()) {
            for (FirebaseVisionText.Line line : block.getLines()) {
                Log.v("LINETEXT", line.getText() + " " + line.getConfidence());
            }
        }
        startTextToSpeech(resultText);
        paintView.setDrawingCacheEnabled(false);
    }

    public void startTextToSpeech(String resultText) {
        Log.v("QUOTE", resultText);
        mTextToSpeech.speak(resultText, TextToSpeech.QUEUE_FLUSH, null, null);
    }
}
