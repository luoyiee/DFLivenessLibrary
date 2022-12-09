package com.liveness.dflivenesslibrary.view;

public interface ITimeViewBase {
	void setProgress(float currentTime);
	void hide();
	void show();
	int getMaxTime();
}