/*
 ** Copyright 2005 Huxtable.com. All rights reserved.
 */

package main.ImageRenderer.Stealing;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

class MotionBlurOp extends AbstractBufferedImageOp {
	private float centreX = 0.5f, centreY = 0.5f;
	private float distance;
	private float angle;
	private float rotation;
	private float zoom;

	public MotionBlurOp() {
	}

	public MotionBlurOp(float distance, float angle, float rotation, float zoom) {
		this.distance = distance;
		this.angle = angle;
		this.rotation = rotation;
		this.zoom = zoom;
	}

	public float getAngle() {
		return angle;
	}

	public void setAngle(float angle) {
		this.angle = angle;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public float getZoom() {
		return zoom;
	}

	public void setZoom(float zoom) {
		this.zoom = zoom;
	}

	public float getCentreX() {
		return centreX;
	}

	public void setCentreX(float centreX) {
		this.centreX = centreX;
	}

	public float getCentreY() {
		return centreY;
	}

	public void setCentreY(float centreY) {
		this.centreY = centreY;
	}

	public Point2D getCentre() {
		return new Point2D.Float(centreX, centreY);
	}

	public void setCentre(Point2D centre) {
		this.centreX = (float) centre.getX();
		this.centreY = (float) centre.getY();
	}

	public BufferedImage filter(BufferedImage src, BufferedImage dst) {
		if (dst == null)
			dst = createCompatibleDestImage(src, null);
		BufferedImage tsrc = src;
		float cx = (float) src.getWidth() * centreX;
		float cy = (float) src.getHeight() * centreY;
		float imageRadius = (float) Math.sqrt(cx * cx + cy * cy);
		float translateX = (float) (distance * Math.cos(angle));
		float translateY = (float) (distance * -Math.sin(angle));
		float scale = zoom;
		float rotate = rotation;
		float maxDistance = distance + Math.abs(rotation * imageRadius) + zoom * imageRadius;
		int steps = log2((int) maxDistance);

		translateX /= maxDistance;
		translateY /= maxDistance;
		scale /= maxDistance;
		rotate /= maxDistance;

		if (steps == 0) {
			Graphics2D g = dst.createGraphics();
			g.drawRenderedImage(src, null);
			g.dispose();
			return dst;
		}

		BufferedImage tmp = createCompatibleDestImage(src, null);
		for (int i = 0; i < steps; i++) {
			Graphics2D g = tmp.createGraphics();
			g.drawImage(tsrc, null, null);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

			g.translate(cx + translateX, cy + translateY);
			g.scale(1.0001 + scale, 1.0001 + scale);  // The .0001 works round a bug on Windows where drawImage throws an ArrayIndexOutofBoundException
			if (rotation != 0)
				g.rotate(rotate);
			g.translate(-cx, -cy);

			g.drawImage(dst, null, null);
			g.dispose();
			BufferedImage ti = dst;
			dst = tmp;
			tmp = ti;
			tsrc = dst;

			translateX *= 2;
			translateY *= 2;
			scale *= 2;
			rotate *= 2;
		}
		return dst;
	}

	private int log2(int n) {
		int m = 1;
		int log2n = 0;

		while (m < n) {
			m *= 2;
			log2n++;
		}
		return log2n;
	}

	public String toString() {
		return "Blur/Motion Blur...";
	}
}