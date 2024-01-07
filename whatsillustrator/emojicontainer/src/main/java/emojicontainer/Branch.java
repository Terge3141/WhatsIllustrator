package emojicontainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Branch {
	private int code;
	private List<Branch> subBranches;
	
	public Branch(int code) {
		this.code = code;
		this.subBranches = new ArrayList<Branch>();
	}

	public void add(List<Integer> path) {
		
		if(path.size() == 0) {
			return;
		}
		
		Branch subBranch;
		int subCode = path.get(0);
		
		Optional<Branch> op = subBranches.stream()
			.filter(x -> x.code==subCode)
			.findFirst();
		
		if(op.isPresent()) {
			subBranch = op.get();
		} else {
			subBranch = new Branch(subCode);
			subBranches.add(subBranch);
		}
		
		subBranch.add(path.subList(1, path.size()));
	}
	
	private void print(int indent) {
		String str = "";
		for(int i=0; i<indent; i++) {
			str += "\t";
		}
		
		System.out.println(str + Integer.toHexString(code));
		for(Branch b : subBranches) {
			b.print(indent + 1);
		}
	}
	
	public void print() {
		print(0);
	}
}
