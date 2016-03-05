package sk.nixone.bwu2.math;

public enum Comparison
{
	LESS {
		@Override
		public boolean compare(int operand1, int operand2)
		{
			return operand1 < operand2;
		}

		@Override
		public boolean compare(double operand1, double operand2)
		{
			return operand1 < operand2;
		}
	},
	LESS_OR_EQUAL {
		@Override
		public boolean compare(int operand1, int operand2)
		{
			return operand1 <= operand2;
		}

		@Override
		public boolean compare(double operand1, double operand2)
		{
			return operand1 <= operand2;
		}
	},
	GREATER {
		@Override
		public boolean compare(int operand1, int operand2)
		{
			return operand1 > operand2;
		}

		@Override
		public boolean compare(double operand1, double operand2)
		{
			return operand1 > operand2;
		}
	},
	GREATER_OR_EQUAL {
		@Override
		public boolean compare(int operand1, int operand2)
		{
			return operand1 >= operand2;
		}

		@Override
		public boolean compare(double operand1, double operand2)
		{
			return operand1 >= operand2;
		}
	},
	EQUAL {
		@Override
		public boolean compare(int operand1, int operand2)
		{
			return operand1 == operand2;
		}

		@Override
		public boolean compare(double operand1, double operand2)
		{
			return operand1 == operand2;
		}
	},
	DIFFERENT {
		@Override
		public boolean compare(int operand1, int operand2)
		{
			return operand1 != operand2;
		}

		@Override
		public boolean compare(double operand1, double operand2)
		{
			return operand1 != operand2;
		}
	};
	
	public abstract boolean compare(int operand1, int operand2);
	public abstract boolean compare(double operand1, double operand2);
}