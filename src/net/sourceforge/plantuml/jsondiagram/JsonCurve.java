/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * Project Info:  http://plantuml.com
 *
 * This file is part of Smetana.
 * Smetana is a partial translation of Graphviz/Dot sources from C to Java.
 *
 * (C) Copyright 2009-2023, Arnaud Roques
 *
 * This translation is distributed under the same Licence as the original C program.
 *
 * THE ACCOMPANYING PROGRAM IS PROVIDED UNDER THE TERMS OF THIS ECLIPSE PUBLIC
 * LICENSE ("AGREEMENT"). [Eclipse Public License - v 1.0]
 *
 * ANY USE, REPRODUCTION OR DISTRIBUTION OF THE PROGRAM CONSTITUTES
 * RECIPIENT'S ACCEPTANCE OF THIS AGREEMENT.
 *
 * You may obtain a copy of the License at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package net.sourceforge.plantuml.jsondiagram;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import h.ST_Agedgeinfo_t;
import h.ST_bezier;
import h.ST_pointf;
import h.ST_splines;
import net.sourceforge.plantuml.ugraphic.UEllipse;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UPath;
import net.sourceforge.plantuml.ugraphic.UStroke;
import net.sourceforge.plantuml.ugraphic.UTranslate;
import net.sourceforge.plantuml.ugraphic.color.HColor;

public class JsonCurve {

	private final List<Point2D> points = new ArrayList<>();
	private double maxX, maxY;
	private final Mirror xMirror;
	private final double veryFirstLine;

	private final Point2D sp;
	private final Point2D ep;

	public JsonCurve(ST_Agedgeinfo_t data, Mirror xMirror, double veryFirstLine) {
		this.veryFirstLine = veryFirstLine;
		this.xMirror = xMirror;
		final ST_splines splines = data.spl;
		if (splines.size != 1) {
			throw new IllegalStateException();
		}
		final ST_bezier beziers = splines.list.get__(0);
		for (int i = 0; i < beziers.size; i++) {
			final Point2D pt = getPoint(splines, i);
			maxX = Math.max(maxX, pt.getX());
			maxY = Math.max(maxY, pt.getY());
			points.add(pt);
		}

		if (beziers.sp.x == 0 && beziers.sp.y == 0) {
			sp = null;
		} else {
			sp = new Point2D.Double(beziers.sp.x, beziers.sp.y);
		}
		if (beziers.ep.x == 0 && beziers.ep.y == 0) {
			ep = null;
		} else {
			ep = new Point2D.Double(beziers.ep.x, beziers.ep.y);
		}

	}

	private Point2D getPoint(ST_splines splines, int i) {
		final ST_bezier beziers = splines.list.get__(0);
		final ST_pointf pt = beziers.list.get__(i);
		return new Point2D.Double(pt.x, pt.y);
	}

	public void drawCurve(HColor color, UGraphic ug) {
		final UPath path = new UPath();

		path.moveTo(getVeryFirst());
		path.lineTo(xMirror.invAndXYSwitch(points.get(0)));

		for (int i = 1; i < points.size(); i += 3) {
			final Point2D pt2 = xMirror.invAndXYSwitch(points.get(i));
			final Point2D pt3 = xMirror.invAndXYSwitch(points.get(i + 1));
			final Point2D pt4 = xMirror.invAndXYSwitch(points.get(i + 2));
			path.cubicTo(pt2, pt3, pt4);
		}
		ug.draw(path);

		if (ep != null) {
			final Point2D last = xMirror.invAndXYSwitch(points.get(points.size() - 1));
			final Point2D trueEp = xMirror.invAndXYSwitch(ep);
			new Arrow(last, trueEp).drawArrow(ug.apply(color.bg()));
		}
	}

	public void drawSpot(UGraphic ug) {
		final double size = 3;
		ug = ug.apply(new UTranslate(getVeryFirst()).compose(new UTranslate(-size, -size)));
		ug.apply(new UStroke()).draw(new UEllipse(2 * size, 2 * size));
	}

	private Point2D getVeryFirst() {
		return supp(xMirror.invAndXYSwitch(points.get(0)), xMirror.invAndXYSwitch(points.get(1)), veryFirstLine);

	}

	private static Point2D supp(Point2D center, Point2D direction, double len) {
		final double full = center.distance(direction);
		final double dx = (center.getX() - direction.getX()) / full;
		final double dy = (center.getY() - direction.getY()) / full;

		return new Point2D.Double(center.getX() + dx * len, center.getY() + dy * len);

	}

	public double getMaxX() {
		return maxX;
	}

	public double getMaxY() {
		return maxY;
	}

}
