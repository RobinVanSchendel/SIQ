package data;

public class XY {
	public int X, Y;
	public XY(int highestPos, Integer integer) {
		this.X = highestPos;
		this.Y = integer;
	}
	public int getX() {
		return X;
	}
	public boolean hasOverlap(XY xYCh2, int minDist) {
		int dis = Math.abs(getX()-xYCh2.getX());
		if(dis<minDist/2){
			return true;
		}
		return false;
	}
	public String getOverlap(XY xYCh2, int minDist) {
		int dis = Math.abs(getX()-xYCh2.getX());
		if(dis<minDist/2){
			return ""+xYCh2.getX();
		}
		return "";
	}

}
