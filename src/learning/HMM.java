package learning;

import java.io.Serializable;
import java.util.List;

public class HMM implements Serializable
{
	private Matrix transitions;
	private Matrix priors;
	private GaussianDistribution[] state;

	public HMM(List<Matrix> trainingSet, List<int[]> labels)
	{
		int n = trainingSet.size();
		state = new GaussianDistribution[n];
		priors = new Matrix(1, n);
		transitions = new Matrix(n, n);

		int obsCount = 0;
		for (int i = 0; i < n; i++)
		{
			state[i] = new GaussianDistribution(trainingSet.get(i));
			obsCount += trainingSet.get(i).rows();
		}

		// for (int i = 0; i < n; i++)
		// {
		// double r = ((double) trainingSet.get(i).rows()) / obsCount;
		// priors.set(0, i, r);
		// }
		for (int i = 0; i < n; i++)
			priors.set(0, i, 1.0 / n);

		if (n > 1)
		{
			if (n < 33)
			{
				for (int i = 0; i < n - 2; i++)
				{
					transitions.set(i, i, 1.0 / 3.0);
					transitions.set(i + 1, i, 1.0 / 3.0);
					transitions.set(i + 2, i, 1.0 / 3.0);
					// transitions.set(i, i, 0.5);
					// transitions.set(i + 1, i, 0.3);
					// transitions.set(i + 2, i, 0.2);

				}
				transitions.set(n - 2, n - 2, 1.0 / 2.0);
				transitions.set(n - 1, n - 2, 1.0 / 2.0);
				transitions.set(n - 1, n - 1, 1.0);
			}
			else
			{
				int[] transCount = new int[n];
				for (int j = 0; j < labels.size(); j++)
				{
					int[] l = labels.get(j);
					for (int i = 0; i < l.length - 1; i++)
					{
						int current = l[i];
						int next = l[i + 1];
						transitions.set(next, current, transitions.get(next, current) + 1);
						transCount[current]++;
					}
				}

				for (int j = 0; j < transitions.cols(); j++)
					for (int i = 0; i < transitions.rows(); i++)
						transitions.set(i, j, transitions.get(i, j) / transCount[j]);

				transitions.set(transitions.rows() - 2, transitions.cols() - 1, 0.1);
				transitions.set(transitions.rows() - 1, transitions.cols() - 1, 0.9);
			}
		}
		else
		{
			// just one state, n=labelCount==1
			transitions.set(0, 0, 1.0);
		}
	}

	public double logsum_pair(double logx, double logy)
	{
		// Return log(x+y), avoiding arithmetic underflow/overflow.
		//
		// logx: log(x)
		// logy: log(y)
		//
		// Rationale:
		//
		// x + y = e^logx + e^logy
		// = e^logx (1 + e^(logy-logx))
		// log(x+y) = logx + log(1 + e^(logy-logx)) (1)
		//
		// Likewise,
		// log(x+y) = logy + log(1 + e^(logx-logy)) (2)
		//
		// The computation of the exponential overflows earlier and is less precise
		// for big values than for small values. Due to the presence of logy-logx
		// (resp. logx-logy), (1) is preferred when logx > logy and (2) is preferred
		// otherwise.

		if (logx == logzero())
		{
			return logy;
		}

		if (logx > logy)
		{
			return logx + Math.log(1.0 + Math.exp(logy - logx));
		}
		else
		{
			return logy + Math.log(1.0 + Math.exp(logx - logy));
		}
	}

	public double logzero()
	{
		return -Double.MAX_VALUE;
	}

	public void print()
	{

		System.out.println("Priors:");
		priors.print();

		System.out.println("Trans:");
		transitions.print();

		for (int i = 0; i < state.length; i++)
			state[i].print();

	}

	public ViterbiResult viterbiMLP(Matrix[] MLPpost, int hmmInd)
	{
		int n = state.length;
		int m = MLPpost[0].rows();
		Matrix t1 = new Matrix(n, m);
		int[][] t2 = new int[n][m];

		// double log_p0=0.0;
		for (int i = 0; i < n; i++)
			t1.set(i, 0, Math.log(priors.get(0, i)) + Math.log(MLPpost[hmmInd].get(0, i)));

		// m = 1;

		for (int i = 1; i < m; i++)
		{

			for (int j = 0; j < n; j++)
			{
				double pmax = Double.NEGATIVE_INFINITY;
				int statemax = -1;
				// System.out.println("Looking for the prob of state " + j + " at frame: " + i);
				for (int k = 0; k < n; k++)
				{
					double transp = Math.log(transitions.get(j, k));

					double prevp = t1.get(k, i - 1);
					double p = transp + prevp;
					// System.out.println("State: " + k + " trans: " + transp + " prevp: " + prevp + " p: " + p);
					if (p > pmax || statemax == -1)
					{
						pmax = p;
						statemax = k;
					}
				}

				t1.set(j, i, pmax + Math.log(MLPpost[hmmInd].get(i, j)));
				t2[j][i] = statemax;
			}

		}

		// t1.print();

		int[] x = new int[m];

		double pmax = Double.NEGATIVE_INFINITY;
		int statemax = 0;

		for (int k = 0; k < n; k++)
		{
			double p = t1.get(k, m - 1);

			if (p > pmax)
			{
				pmax = p;
				statemax = k;
			}
		}

		x[m - 1] = statemax;
		for (int i = m - 1; i > 0; i--)
			x[i - 1] = t2[x[i]][i];

		return new ViterbiResult(pmax, x);
	}

	public ViterbiResult viterbi(Matrix obs)
	{
		int n = state.length;
		int m = obs.rows();
		Matrix t1 = new Matrix(n, m);
		int[][] t2 = new int[n][m];

		// double log_p0=0.0;
		for (int i = 0; i < n; i++)
			t1.set(i, 0, Math.log(priors.get(0, i)) + state[i].log_likelihood(obs.getRowMatrix(0)));

		// t1.set(i, 0, Math.log(priors.get(0, i)) + state[i].likelihood(obs.getRowMatrix(0)));
		// Matrix obs1 = new Matrix(new double[][]{{1.2}, {1}, {1.5}, {4}, {5}, {3.1}, {1}, {1.2}});
		// obs1.print();
		// int[] r1 = hmm.viterbi(obs1);
		// System.out.println("Path: " + Arrays.toString(r1));
		//
		// Matrix obs2 = new Matrix(new double[][]{{4}, {5}, {3.1}, {1}, {1.2}, {1.2}, {1}, {1.5}});
		// obs2.print();
		// int[] r2 = hmm.viterbi(obs2);
		// System.out.println("Path: " + Arrays.toString(r2));

		// m = 1;
		for (int i = 1; i < m; i++)
		{
			Matrix currentObs = obs.getRowMatrix(i);
			// System.out.println("Obs: " + i);

			for (int j = 0; j < n; j++)
			{
				double pmax = Double.NEGATIVE_INFINITY;
				int statemax = -1;
				// System.out.println("Looking for the prob of state " + j + " at frame: " + i);
				for (int k = 0; k < n; k++)
				{
					double transp = Math.log(transitions.get(j, k));

					double prevp = t1.get(k, i - 1);
					double p = transp + prevp;
					// System.out.println("State: " + k + " trans: " + transp + " prevp: " + prevp + " p: " + p);
					if (p > pmax || statemax == -1)
					{
						pmax = p;
						statemax = k;
					}
				}

				t1.set(j, i, pmax + state[j].log_likelihood(currentObs));
				t2[j][i] = statemax;
			}

		}

		int[] x = new int[m];

		double pmax = Double.NEGATIVE_INFINITY;
		int statemax = 0;

		for (int k = 0; k < n; k++)
		{
			double p = t1.get(k, m - 1);

			if (p > pmax)
			{
				pmax = p;
				statemax = k;
			}
		}

		x[m - 1] = statemax;
		for (int i = m - 1; i > 0; i--)
			x[i - 1] = t2[x[i]][i];

		// t1.print();
		// for (int j = 0; j < n; j++)
		// {
		// System.out.println(Arrays.toString(t2[j]));
		// }
		return new ViterbiResult(pmax, x);
	}

	public ViterbiResult viterbiAlign(Matrix obs, int gLab)
	{
		int n = state.length;
		int m = obs.rows();
		Matrix t1 = new Matrix(n, m);
		int[][] t2 = new int[n][m];

		// double log_p0=0.0;
		for (int i = 0; i < n; i++)
			t1.set(i, 0, Math.log(priors.get(0, i)) + state[i].log_likelihood(obs.getRowMatrix(0)));

		// m = 1;
		for (int i = 1; i < m; i++)
		{
			Matrix currentObs = obs.getRowMatrix(i);
			// System.out.println("Obs: " + i);

			for (int j = 0; j < n; j++)
			{
				double pmax = Double.NEGATIVE_INFINITY;
				int statemax = -1;
				// System.out.println("Looking for the prob of state " + j + " at frame: " + i);
				for (int k = 0; k < n; k++)
				{
					double transp = Math.log(transitions.get(j, k));

					double prevp = t1.get(k, i - 1);
					double p = transp + prevp;
					// System.out.println("State: " + k + " trans: " + transp + " prevp: " + prevp + " p: " + p);
					if (p > pmax || statemax == -1)
					{
						pmax = p;
						statemax = k;
					}
				}

				t1.set(j, i, pmax + state[j].log_likelihood(currentObs));
				t2[j][i] = statemax;
			}

		}

		int[] x = new int[m];

		// on force a choisir le dernier etat a t=n:
		double pmax = t1.get(n - 1, m - 1);
		int statemax = n - 1;

		x[m - 1] = statemax;
		for (int i = m - 1; i > 0; i--)
			x[i - 1] = t2[x[i]][i];

		// if(gLab == 0 || gLab == 20){
		//
		// System.out.print(gLab + " " + pmax + " ");
		//
		// for (int j = 0; j < m; j++)
		// System.out.print(x[j] + " ");
		// System.out.println("");
		//
		// // for (int j = 0; j < n; j++)
		// // System.out.println(Arrays.toString(t2[j]));
		// }

		return new ViterbiResult(pmax, x);
	}

	public class ViterbiResult
	{
		public double score;
		public int[] path;

		public ViterbiResult(double s, int[] p)
		{
			score = s;
			path = p;
		}
	}

	public ViterbiResult forward(Matrix obs)
	{
		// System.out.println("Transition Matrix: ");
		// transitions.transpose().print();

		// System.out.println("Priors Matrix: ");
		// priors.print();

		int n = state.length;
		int m = obs.rows();
		Matrix t1 = new Matrix(n, m);

		// double p_denom = 0;
		double[] alpha = new double[n];

		for (int i = 0; i < n; i++)
		{
			// alpha[i] = priors.get(0, i) * state[i].likelihood(obs.getRowMatrix(0));
			alpha[i] = Math.log(priors.get(0, i)) + state[i].log_likelihood(obs.getRowMatrix(0));
			// p_denom += alpha[i];
			t1.set(i, 0, alpha[i]);

		}

		// for (int i = 0; i < n; i++){
		// t1.set(i, 0, alpha[i]);
		// // t1.set(i, 0, alpha[i]/p_denom);
		// // System.out.println("lh (" + i + ") :  " + state[i].likelihood(obs.getRowMatrix(0)) + " alpha: " + alpha[i] + " alpha_norm: " + alpha[i]/p_denom);
		// }

		// t1.print();

		// m = 1;
		for (int i = 1; i < m; i++)
		{
			Matrix currentObs = obs.getRowMatrix(i);
			// System.out.println("Obs: " + i);

			// p_denom = 0;

			for (int j = 0; j < n; j++)
			{
				// double alpha_a = 0;
				// double modelp = state[j].likelihood(currentObs);

				double alpha_acc = logzero(); // == -Double.MAX_VALUE;
				double modelp = state[j].log_likelihood(currentObs);

				for (int k = 0; k < n; k++)
				{
					double transp = transitions.get(j, k);
					double alpha_prev = t1.get(k, i - 1);
					// alpha_acc += alpha_prev * transp;
					alpha_acc = logsum_pair(alpha_acc, alpha_prev + Math.log(transp));

				}

				// alpha[j] = alpha_acc * modelp;
				// p_denom += alpha[j];
				alpha[j] = alpha_acc + modelp;
			}

			for (int j = 0; j < n; j++)
			{
				t1.set(j, i, alpha[j]);
				// t1.set(j, i, alpha[j]/p_denom);
				// System.out.println("lh (" + j + ") :  " + state[j].likelihood(obs.getRowMatrix(0)) + " alpha: " + alpha[j] + " alpha_norm: " + alpha[j]/p_denom);
			}
		}

		// System.out.println("T1: ");
		// t1.print();

		// double score = Math.log(p_denom);
		// double score = 0;
		// for (int j = 0; j < n; j++)
		// score += t1.get(j, m - 1);

		double score = logzero(); // == -Double.MAX_VALUE;
		for (int j = 0; j < n; j++)
			score = logsum_pair(score, t1.get(j, m - 1));

		return new ViterbiResult(score, null);
	}

	public ViterbiResult forwardMLP(Matrix[] MLPpost, int hmmInd)
	{
		// System.out.println("Transition Matrix: ");
		// transitions.transpose().print();

		// System.out.println("Priors Matrix: ");
		// priors.print();

		int n = state.length;
		// System.out.println("HMM: " + hmmInd + "STATELENGTH= " + n);
		int m = MLPpost[0].rows();
		Matrix t1 = new Matrix(n, m);

		double p_denom = 0;
		double[] alpha = new double[n];

		for (int i = 0; i < n; i++)
		{
			// System.out.println("priors:" + priors.get(0, i));
			// System.out.println("post:" + MLPpost[hmmInd].get(0, i));

			alpha[i] = priors.get(0, i) * MLPpost[hmmInd].get(0, i);
			p_denom += alpha[i];
		}

		for (int i = 0; i < n; i++)
		{
			t1.set(i, 0, alpha[i]);
			// t1.set(i, 0, alpha[i]/p_denom);
			// System.out.println("lh (" + i + ") :  " + state[i].likelihood(obs.getRowMatrix(0)) + " alpha: " + alpha[i] + " alpha_norm: " + alpha[i]/p_denom);
		}

		// t1.print();

		// m = 1;
		for (int i = 1; i < m; i++)
		{
			p_denom = 0;

			for (int j = 0; j < n; j++)
			{
				double alpha_a = 0;
				double modelp = MLPpost[hmmInd].get(i, j);

				for (int k = 0; k < n; k++)
				{
					double transp = transitions.get(j, k);
					double alpha_prev = t1.get(k, i - 1);
					alpha_a += alpha_prev * transp;
					// System.out.println("Modelp : " + modelp + " transp : " + transp + " prevp : " + prevp + " p : " + p);
				}

				alpha[j] = alpha_a * modelp;
				p_denom += alpha[j];
			}

			for (int j = 0; j < n; j++)
				t1.set(j, i, alpha[j]);
			// t1.set(j, i, alpha[j]/p_denom);
		}

		// System.out.println("T1: ");
		// t1.print();

		double score = 0;
		for (int j = 0; j < n; j++)
			score += t1.get(j, m - 1);

		return new ViterbiResult(score, null);
	}

	public ViterbiResult forwardMLPLog(Matrix[] MLPpost, int indHMM)
	{

		// Uses a single HMM-state-based MLP

		// System.out.println("Transition Matrix: ");
		// transitions.transpose().print();

		// System.out.println("Priors Matrix: ");
		// priors.print();

		int n = state.length;
		// System.out.println("HMM: " + hmmInd + "STATELENGTH= " + n);
		int m = MLPpost[0].rows();
		Matrix t1 = new Matrix(n, m);

		double[] alpha = new double[n];

		for (int i = 0; i < n; i++)
		{
			// System.out.println("priors:" + priors.get(0, i));
			// System.out.println("post:" + MLPpost[hmmInd].get(0, i));
			if (indHMM < 20)
			{
				alpha[i] = Math.log(MLPpost[0].get(0, i + indHMM * state.length));
				// alpha[i] = Math.log(priors.get(0, i)) + Math.log(MLPpost[0].get(0, i + indHMM * state.length));
			}
			else
			{
				alpha[i] = Math.log(MLPpost[0].get(0, i + indHMM * 3)); // col 60 pour le geste 21 qui n'a qu'un etat
				// alpha[i] = Math.log(priors.get(0, i)) + Math.log(MLPpost[0].get(0, i + indHMM * 3)); // col 60 pour le geste 21 qui n'a qu'un etat
			}
			t1.set(i, 0, alpha[i]);
		}
		// t1.print();

		// m = 1;
		for (int i = 1; i < m; i++)
		{
			for (int j = 0; j < n; j++)
			{

				double alpha_acc = logzero(); // == -Double.MAX_VALUE;
				double modelp = 0;

				if (indHMM < 20)
				{
					// modelp = Math.log(MLPpost[0].get(i, j + indHMM * state.length));
					modelp = Math.log(MLPpost[0].get(i, j + indHMM * state.length)) - Math.log(priors.get(0, j));
				}
				else
				{
					modelp = Math.log(MLPpost[0].get(i, j + indHMM * 3)) - Math.log(priors.get(0, j));// col 60 pour le geste 21 (1 etat)
					// modelp = Math.log(MLPpost[0].get(i, j + indHMM * 3));// col 60 pour le geste 21 (1 etat)
				}

				for (int k = 0; k < n; k++)
				{
					double transp = Math.log(transitions.get(j, k));
					double alpha_prev = t1.get(k, i - 1);
					// alpha_acc += alpha_prev * transp;
					alpha_acc = logsum_pair(alpha_acc, alpha_prev + transp);

				}

				alpha[j] = alpha_acc + modelp;

			}

			for (int j = 0; j < n; j++)
				t1.set(j, i, alpha[j]);
			// t1.set(j, i, alpha[j]/p_denom);
		}

		// System.out.println("T1: ");
		// t1.print();

		// double score = Math.log(p_denom);
		// double score = 0;
		// for (int j = 0; j < n; j++)
		// score += t1.get(j, m - 1);

		double score = logzero(); // == -Double.MAX_VALUE;
		for (int j = 0; j < n; j++)
			score = logsum_pair(score, t1.get(j, m - 1));

		return new ViterbiResult(score, null);
	}

	public static int[] generateStateLabels(int captureCount, boolean repeatFirst, double... weights)
	{
		int[] labels = new int[captureCount];
		double sum = 0;
		for (double w : weights)
			sum += w;

		for (int i = 0; i < weights.length; i++)
			weights[i] /= sum;

		int framesLabeled = 0;

		boolean singleState = (weights.length == 1 ? true : false);

		int currentState = 0;
		double frameWeight = 1.0 / captureCount;

		for (int i = 0; i < captureCount; i++)
		{
			framesLabeled++;
			if ((!singleState) && weights[currentState] <= frameWeight * framesLabeled)
			{
				currentState++;
				framesLabeled = 0;
			}
			labels[i] = currentState;
		}

		return labels;
	}
}
