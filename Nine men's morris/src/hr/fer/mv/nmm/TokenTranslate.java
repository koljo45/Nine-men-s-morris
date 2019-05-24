package hr.fer.mv.nmm;

import java.awt.Point;

public class TokenTranslate extends AnimationTranslate {

	private TokenSkin ts;

	public TokenTranslate(int stepNumber, Point startPoint, Point endPoint, TokenSkin ts) {
		super(stepNumber, startPoint, endPoint);
		this.ts = ts;
	}

	public TokenSkin getTokenSkin() {
		return ts;
	}

}
