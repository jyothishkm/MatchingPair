package com.matchingpairs;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    LinearLayout mQuestionLayout;
    LinearLayout mAnswerLayout;
    DrawView mDraw;
    List<String> questionsList = new ArrayList<>();
    List<String> answersList = new ArrayList<>();
    List<String> answerListCmp = new ArrayList<>();
    int mSubtractionCoordinatesIntArray[] = new int[2];
    private static int QUESTION_ID = 100;
    private static int ANSWER_ID = 200;
    private static final String TAG = MainActivity.class.getSimpleName();

    ArrayList<Integer> mSelectedItemsIdArray;
    List<View> mSelectedListViews;
    int mStartCoordinatesIntArray[] = new int[2];
    int mEndCoordinatesIntArray[] = new int[2];
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        if (savedInstanceState != null) {

        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        loadJSONFromAsset();
        readJson();
        setViews();

        mSelectedItemsIdArray = new ArrayList<>();
        mSelectedListViews = new ArrayList<>();

    }

    private void initView()
    {
        mQuestionLayout = (LinearLayout) findViewById(R.id.question_layout);
        mAnswerLayout = (LinearLayout) findViewById(R.id.answer_layout);
        mDraw = (DrawView) findViewById(R.id.draw_view);
    }

    private void setViews()
    {
        addQuestionViews();
        addAnswerViews();
    }

    private void addQuestionViews()
    {

        for (String question : questionsList)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.question_item, null);
            view.setId(QUESTION_ID++);
            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(question);
            view.setOnClickListener(this);
            mQuestionLayout.addView(view);
        }
    }

    private void addAnswerViews()
    {


        for (String answer: answersList)
        {
            View view = LayoutInflater.from(this).inflate(R.layout.answer_item, null);
            view.setId(ANSWER_ID++);
            TextView textView = (TextView) view.findViewById(R.id.text);
            textView.setText(answer);
            view.setOnClickListener(this);
            mAnswerLayout.addView(view);
        }
    }

    @Override
    public void onClick(View v)
    {
        mQuestionLayout.getLocationOnScreen(mSubtractionCoordinatesIntArray);
        /*Log.i(TAG, "initView: question layout coordinates ("+mSubtractionCoordinatesIntArray[0]+
                ", "+mSubtractionCoordinatesIntArray[1]+")");*/
        ImageView nodeImage = (ImageView) v.findViewById(R.id.node);
        int xy[] = new int[2];
        nodeImage.getLocationInWindow(xy);
        //Log.i(TAG, "onClick: circle position--> x-> "+xy[0]+" y-> "+xy[1]);
        Log.i(TAG, "onClick: id--> "+v.getId());

        if(!mSelectedItemsIdArray.contains(v.getId()))
        {
            mSelectedListViews.add(v);
            mSelectedItemsIdArray.add(v.getId());
            if(mSelectedListViews.size() == 2)
            {
                if(v.getId() >= 100 && v.getId() <= QUESTION_ID)
                {
                    mEndCoordinatesIntArray[0] = 0;
                    mEndCoordinatesIntArray[1] = (xy[1] - mSubtractionCoordinatesIntArray[1] + (nodeImage.getHeight() / 2));
                }
                else
                {
                    mEndCoordinatesIntArray[0] = mDraw.getWidth();
                    mEndCoordinatesIntArray[1] = (xy[1] - mSubtractionCoordinatesIntArray[1] + (nodeImage.getHeight() / 2));
                }
                comparePair();
            }
            else
            {
                setItemSelected(v);
                if(v.getId() >= 100 && v.getId() <= QUESTION_ID)
                {
                    mStartCoordinatesIntArray[0] = 0;
                    mStartCoordinatesIntArray[1] = (xy[1] - mSubtractionCoordinatesIntArray[1] + (nodeImage.getHeight() / 2));
                }
                else
                {
                    mStartCoordinatesIntArray[0] = mDraw.getWidth();
                    mStartCoordinatesIntArray[1] = (xy[1] - mSubtractionCoordinatesIntArray[1] + (nodeImage.getHeight() / 2));
                }
            }
        }


    }
    private void setItemSelected(View view)
    {
        ImageView border = (ImageView) view.findViewById(R.id.border);
        ImageView node = (ImageView) view.findViewById(R.id.node);

        border.setImageResource(R.drawable.selected_border_shape);
        node.setImageResource(R.drawable.selected_circle);
    }

    private void comparePair()
    {
        String text1, text2;
        TextView textView1 = (TextView) mSelectedListViews.get(0).findViewById(R.id.text);
        TextView textView2 = (TextView) mSelectedListViews.get(1).findViewById(R.id.text);
        text1 = textView1.getText().toString();
        text2 = textView2.getText().toString();
        if(answerListCmp.contains(text1+text2) || answerListCmp.contains(text2+text1))
        {
            for(View view : mSelectedListViews)
            {
                ImageView border = (ImageView) view.findViewById(R.id.border);
                ImageView node = (ImageView) view.findViewById(R.id.node);
                border.setImageResource(R.drawable.right_answer_border);
                node.setImageResource(R.drawable.right_answer_circle);
            }
            mDraw.drawLine(new LineCoordinate(mStartCoordinatesIntArray[0], mStartCoordinatesIntArray[1],
                    mEndCoordinatesIntArray[0], mEndCoordinatesIntArray[1]));
            mSelectedListViews.clear();
        }
        else
        {
            for(View view : mSelectedListViews)
            {
                ImageView border = (ImageView) view.findViewById(R.id.border);
                ImageView node = (ImageView) view.findViewById(R.id.node);
                border.setImageResource(R.drawable.wrong_answer_border);
                node.setImageResource(R.drawable.wrong_answer_circle);
            }

            new Timer().schedule(new TimerTask() {
                @Override
                public void run()
                {
                    UiThread();
                }
            }, 1000);
        }
    }

    private void UiThread()
    {
        new Thread(){
            @Override
            public void run()
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run()
                    {
                        for(View view : mSelectedListViews)
                        {
                            ImageView border = (ImageView) view.findViewById(R.id.border);
                            ImageView node = (ImageView) view.findViewById(R.id.node);
                            border.setImageResource(R.drawable.outer_border_shape);
                            node.setImageResource(R.drawable.ring_shape);
                            mSelectedItemsIdArray.remove((Object)view.getId());
                        }
                        mSelectedListViews.clear();
                    }
                });
            }
        }.start();
    }


    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("jsonData.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        Log.i(TAG, "loadJSONFromAsset: "+json);
        return json;
    }
    private void readJson() {
        JSONObject jsonObject = null;
        JSONArray jsonArry = null;
        JSONObject childJson = null;

        try {
            jsonObject = new JSONObject(loadJSONFromAsset());
            jsonArry = jsonObject.getJSONArray("QUESTION");

            for (int i = 0; i <jsonArry.length() ; i++) {

                childJson = jsonArry.getJSONObject(i);
                questionsList.add(childJson.getString("q_data"));
            }

            jsonArry = jsonObject.getJSONArray("ANSWER");
            for (int i = 0; i <jsonArry.length() ; i++) {

                childJson = jsonArry.getJSONObject(i);
                answersList.add(childJson.getString("a_data"));
            }
            jsonArry = jsonObject.getJSONArray("ANSWERLIST");
            for (int i = 0; i <jsonArry.length() ; i++) {

                childJson = jsonArry.getJSONObject(i);
                answerListCmp.add(childJson.getString("c_data"));
            }
        }catch (JSONException e) {

            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
    }
}
