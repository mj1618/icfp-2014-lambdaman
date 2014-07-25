package ghc.interpret;

public class Argument {

	ArgumentType type;
	
	//One of these
	Character register;
	int number;
	
	public Argument(){}
	
	
	public Argument(ArgumentType type, Character register, int number) {
		super();
		this.type = type;
		this.register = register;
		this.number = number;
	}


	@Override
	public String toString() {
		return "Argument [type=" + type + ", register=" + register
				+ ", number=" + number + "]";
	}


	public ArgumentType getType() {
		return type;
	}
	public void setType(ArgumentType type) {
		this.type = type;
	}
	public Character getRegister() {
		return register;
	}
	public void setRegister(Character register) {
		this.register = register;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
}
