package learning;

import java.io.Serializable;

public class GaussianDistribution implements Serializable
{
	private Matrix mean;
	private Matrix cov;
	private Matrix covInv;
	private double logCovDet;
	private double covDet;

	public GaussianDistribution(Matrix set)
	{
//		set.printDimensions();
		mean = set.mean();
		cov = set.covariance(mean);
		covDet = cov.det();
		//System.out.println("CovDet computed: " + covDet);
		logCovDet = Math.log(Math.abs(covDet));
		covInv = cov.inv();
		//mean.print();
		// covInv.print();
		
	}

	public double likelihood(Matrix obs)
	{
		Matrix d = Matrix.substract(obs, mean);
		int k = d.cols();
		
		Matrix m1 = d.multiply(covInv);
		Matrix m2 = m1.multiply(d.transpose());

//		return Math.exp(-0.5 * m2.get(0, 0));
		return Math.exp(-0.5 * m2.get(0, 0)) / (Math.pow(Math.sqrt(2 * Math.PI), k) * Math.sqrt(Math.abs(covDet)));
//		return - (logCovDet + m2.get(0, 0) + k * Math.log(Math.PI) * 2) / 2;
//		return Math.exp(-(m2.get(0, 0) / 2)) / (Math.pow(Math.sqrt(2 * Math.PI), k) + covDet);
	}

	
	public double log_likelihood(Matrix obs)
	{
		Matrix d = Matrix.substract(obs, mean);
//		int k = d.cols();
		
		Matrix m1 = d.multiply(covInv);
		Matrix m2 = m1.multiply(d.transpose());
		
		//System.out.print("likelihood : ");
		//d.print();
		//m1.print();
		//obs.print();
		//cov.print();
		//covInv.print();
		//m1.print();
		
//		m2.print();
//		System.out.println("m2=" + -m2.get(0, 0)/2);
		
		return - ( logCovDet + m2.get(0, 0)) / 2;
//		return - (logCovDet + m2.get(0, 0) + k * Math.log(Math.PI) * 2) / 2;
//		return Math.exp(-(m2.get(0, 0) / 2)) / (Math.pow(Math.sqrt(2 * Math.PI), k) + covDet);
	}

	
	
	
	public void print()
	{
		System.out.println("Mean: ");
		this.mean.print();
		
//		System.out.println("Covariance: ");
//		this.cov.print();
//		
//		System.out.println("InvCovariance: ");
//		this.covInv.print();
		
	}
}
