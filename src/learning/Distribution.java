package learning;

public interface Distribution
{	
	public abstract double likelihood(Matrix obs);
	public abstract double likelihoodLog(Matrix obs);
	
	public abstract void print();
}
