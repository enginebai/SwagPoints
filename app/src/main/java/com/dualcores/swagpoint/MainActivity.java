package com.dualcores.swagpoint;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.dualcores.swagpoints.SwagPoints;

public class MainActivity extends AppCompatActivity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final SwagPoints swagPoints = (SwagPoints) this.findViewById(R.id.swag_points);
		final TextView mTextPoints = (TextView)this.findViewById(R.id.text_points);

		swagPoints.setOnSwagPointsChangeListener(new SwagPoints.OnSwagPointsChangeListener() {
			@Override
			public void onPointsChanged(SwagPoints swagPoints, int point, boolean fromUser) {
				mTextPoints.setText(String.valueOf(point));
				Log.d(MainActivity.this.getClass().getSimpleName(), String.valueOf(point));
			}

			@Override
			public void onStartTackingTouch(SwagPoints swagPoints) {

			}

			@Override
			public void onStopTrackingTouch(SwagPoints swagPoints) {

			}
		});
	}
}
