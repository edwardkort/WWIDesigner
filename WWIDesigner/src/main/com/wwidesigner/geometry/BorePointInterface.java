package com.wwidesigner.geometry;


public interface BorePointInterface
{

	public abstract BoreSectionInterface getBoreSection();

	public abstract void setBoreSection(BoreSectionInterface boreSection);

	public abstract double getDiameter();

	public abstract void setDiameter(double diameter);

	public abstract HoleInterface getHole();

	public abstract void setHole(HoleInterface hole);

}