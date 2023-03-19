import java.util.*;


record Point(double x, double y) {

  static double distance(final Point pointA, final Point pointB) {
    double dx = pointA.x() - pointB.x();
    double dy = pointA.y() - pointB.y();
    return Math.sqrt(dx * dx + dy * dy);
  }

  static double dotProduct(final Point pointA, final Point pointB) {
    return pointA.x() * pointB.x() + pointA.y() * pointB.y();
  }

  static Point ZERO = new Point(0, 0);

  public Point add(final Point value) {
    return new Point(this.x + value.x(), this.y + value.y());
  }

  public Point subtract(final Point value) {
    return new Point(this.x - value.x(), this.y - value.y());
  }

  public Point multiply(final double coefficient) {
    return new Point(this.x * coefficient, this.y * coefficient);
  }

  public Point divide(final double coefficient) {
    return new Point(this.x / coefficient, this.y / coefficient);
  }

  public Point rotate(Point ref, double angle) {
    double dx = this.x - ref.x();
    double dy = this.y - ref.y();
    double cos = Math.cos(angle);
    double sin = Math.sin(angle);
    return new Point(ref.x() + cos * dx - sin * dy, ref.y() + sin * dx + cos * dy);
  }
}


abstract class Shape {

  public abstract Point center();

  public abstract double perimeter();

  public abstract double area();

  public abstract void translate(final Point newCenter);

  public abstract void rotate(final double angle);

  public abstract void scale(final double coefficient);
}


class Ellipse extends Shape {

  protected Point focusOne;
  protected Point focusTwo;
  protected Point barycenter;
  protected double perifocalDistance;

  public Ellipse(final Point pointA, final Point pointB, final double distance) {
    focusOne = pointA;
    focusTwo = pointB;
    perifocalDistance = distance;
    barycenter = pointA.add(pointB).divide(2.0);
  }

  public List<Point> focuses() {
    List<Point> result = new ArrayList<>();
    result.add(focusOne);
    result.add(focusTwo);
    return result;
  }

  public double focalDistance() {
    return Point.distance(focusOne, barycenter);
  }

  public double majorSemiAxis() {
    return focalDistance() + perifocalDistance;
  }

  public double minorSemiAxis() {
    double dst = focalDistance();
    double mjrAxis = majorSemiAxis();
    return Math.sqrt(mjrAxis * mjrAxis - dst * dst);
  }

  public double eccentricity() {
    return focalDistance() / majorSemiAxis();
  }

  public Point center() {
    return barycenter;
  }

  public double perimeter() {
    double a = majorSemiAxis();
    double b = minorSemiAxis();
    return 4 * (Math.PI * a * b + (a - b) * (a - b)) / (a + b);
  }

  public double area() {
    return Math.PI * majorSemiAxis() * minorSemiAxis();
  }

  public void translate(final Point newCenter) {
    Point diff = newCenter.subtract(barycenter);
    focusOne = focusOne.add(diff);
    focusTwo = focusTwo.add(diff);
    barycenter = newCenter;
  }

  public void rotate(final double angle) {
    focusOne = focusOne.rotate(barycenter, angle);
    focusTwo = focusTwo.rotate(barycenter, angle);
  }

  public void scale(final double coefficient) {
    perifocalDistance *= Math.abs(coefficient);
    Point oldBarycenter = barycenter;
    translate(Point.ZERO);
    focusOne = focusOne.multiply(coefficient);
    focusTwo = focusTwo.multiply(coefficient);
    translate(oldBarycenter);
  }
}


class Circle extends Ellipse {

  public Circle(final Point center, final double radius) {
    super(center, center, radius);
  }

  public double radius() {
    return perifocalDistance;
  }
}


class Rectangle extends Shape {

  protected Point pointA;
  protected Point pointB;
  protected Point barycenter;
  protected double sideOne;
  protected double sideTwo;

  public Rectangle(final Point first, final Point second, final double side) {
    pointA = first;
    pointB = second;
    barycenter = pointA.add(pointB).divide(2.0);
    sideOne = Point.distance(pointA, pointB);
    sideTwo = side;
  }

  public List<Point> vertices() {
    Point vectorAB = new Point(pointB.x() - pointA.x(), pointB.y() - pointA.y());
    Point vectorOX = new Point(1, 0);
    double cosAlpha = Point.dotProduct(vectorAB, vectorOX) / Point.distance(pointA, pointB);
    double angel = pointA.y() > pointB.y() ? -Math.acos(cosAlpha) : Math.acos(cosAlpha);

    List<Point> result = new ArrayList<>();
    rotate(-angel);
    result.add(new Point(pointA.x(), pointA.y() - sideTwo / 2).rotate(barycenter, angel));
    result.add(new Point(pointB.x(), pointB.y() - sideTwo / 2).rotate(barycenter, angel));
    result.add(new Point(pointB.x(), pointB.y() + sideTwo / 2).rotate(barycenter, angel));
    result.add(new Point(pointA.x(), pointA.y() + sideTwo / 2).rotate(barycenter, angel));
    rotate(angel);
    return result;
  }

  public double firstSide() {
    return sideOne;
  }

  public double secondSide() {
    return sideTwo;
  }

  public double diagonal() {
    return Math.sqrt(sideOne * sideOne + sideTwo * sideTwo);
  }

  public Point center() {
    return barycenter;
  }

  public double perimeter() {
    return 2 * (sideOne + sideTwo);
  }

  public double area() {
    return sideOne * sideTwo;
  }

  public void translate(final Point newCenter) {
    Point diff = newCenter.subtract(barycenter);
    pointA = pointA.add(diff);
    pointB = pointB.add(diff);
    barycenter = newCenter;
  }

  public void rotate(final double angle) {
    pointA = pointA.rotate(barycenter, angle);
    pointB = pointB.rotate(barycenter, angle);
  }

  public void scale(final double coefficient) {
    Point oldBarycenter = barycenter;
    sideOne *= Math.abs(coefficient);
    sideTwo *= Math.abs(coefficient);
    translate(Point.ZERO);
    pointA = pointA.multiply(coefficient);
    pointB = pointB.multiply(coefficient);
    translate(oldBarycenter);
  }
}


class Square extends Rectangle {

  public Square(Point a, Point b) {
    super(a, b, Point.distance(a, b));
  }

  public double side() {
    return sideOne;
  }

  public Circle circumscribedCircle() {
    return new Circle(barycenter, diagonal() / 2);
  }

  public Circle inscribedCircle() {
    return new Circle(barycenter, side() / 2);
  }
}


class Triangle extends Shape {

  protected Point pointA;
  protected Point pointB;
  protected Point pointC;
  protected Point barycenter;

  public Triangle(final Point a, final Point b, final Point c) {
    pointA = a;
    pointB = b;
    pointC = c;
    barycenter = new Point((a.x() + b.x() + c.x()) / 3, (a.y() + b.y() + c.y()) / 3);
  }

  public List<Point> vertices() {
    List<Point> result = new ArrayList<>();
    result.add(pointA);
    result.add(pointB);
    result.add(pointC);
    return result;
  }

  private Point circumscribedCircleCenter() {
    double xab = pointA.x() - pointB.x();
    double yab = pointA.y() - pointB.y();
    double xbc = pointB.x() - pointC.x();
    double ybc = pointB.y() - pointC.y();
    double xca = pointC.x() - pointA.x();
    double yca = pointC.y() - pointA.y();
    double z = xab * yca - yab * xca;
    double z1 = pointA.x() * pointA.x() + pointA.y() * pointA.y();
    double z2 = pointB.x() * pointB.x() + pointB.y() * pointB.y();
    double z3 = pointC.x() * pointC.x() + pointC.y() * pointC.y();
    double zx = yab * z3 + ybc * z1 + yca * z2;
    double zy = xab * z3 + xbc * z1 + xca * z2;
    return new Point(-zx / 2 / z, zy / 2 / z);
  }

  private double circumscribedCircleRadius() {
    double sideAB = Point.distance(pointA, pointB);
    double sideAC = Point.distance(pointA, pointC);
    double sideBC = Point.distance(pointB, pointC);
    return sideAB * sideBC * sideAC / 4 / area();
  }

  public Circle circumscribedCircle() {
    return new Circle(circumscribedCircleCenter(), circumscribedCircleRadius());
  }

  public Circle inscribedCircle() {
    double sideA = Point.distance(pointB, pointC);
    double sideB = Point.distance(pointA, pointC);
    double sideC = Point.distance(pointA, pointB);
    double sumSides = sideA + sideB + sideC;
    double p = perimeter() / 2;

    double radius = Math.sqrt((p - sideA) * (p - sideB) * (p - sideC) / p);
    double x = (sideA * pointA.x() + sideB * pointB.x() + sideC * pointC.x()) / sumSides;
    double y = (sideA * pointA.y() + sideB * pointB.y() + sideC * pointC.y()) / sumSides;

    return new Circle(new Point(x, y), radius);
  }

  public Point orthocenter() {
    double xcb = pointC.x() - pointB.x();
    double ycb = pointC.y() - pointB.y();
    double xca = pointC.x() - pointA.x();
    double yca = pointC.y() - pointA.y();
    double valcb = xcb * pointA.x() + ycb * pointA.y();
    double valca = xca * pointB.x() + yca * pointB.y();

    double det = xcb * yca - xca * ycb;
    double detX = valcb * yca - valca * ycb;
    double detY = xcb * valca - xca * valcb;

    return new Point(detX / det, detY / det);
  }

  public Circle ninePointsCircle() {
    Point point = orthocenter().add(circumscribedCircleCenter()).divide(2.0);
    double radius = circumscribedCircleRadius() / 2;
    return new Circle(point, radius);
  }

  public Point center() {
    return barycenter;
  }

  public double perimeter() {
    double sideAB = Point.distance(pointA, pointB);
    double sideAC = Point.distance(pointA, pointC);
    double sideBC = Point.distance(pointB, pointC);
    return sideAB + sideBC + sideAC;
  }

  public double area() {
    return 0.5 * Math.abs(
        (pointB.x() - pointA.x()) * (pointC.y() - pointA.y())
            - (pointC.x() - pointA.x()) * (pointB.y() - pointA.y())
    );
  }

  public void translate(final Point newCenter) {
    Point diff = newCenter.subtract(barycenter);
    pointA = pointA.add(diff);
    pointB = pointB.add(diff);
    pointC = pointC.add(diff);
    barycenter = newCenter;
  }

  public void rotate(final double angle) {
    pointA = pointA.rotate(barycenter, angle);
    pointB = pointB.rotate(barycenter, angle);
    pointC = pointC.rotate(barycenter, angle);
  }

  public void scale(final double coefficient) {
    Point oldBarycenter = barycenter;
    translate(Point.ZERO);
    pointA = pointA.multiply(coefficient);
    pointB = pointB.multiply(coefficient);
    pointC = pointC.multiply(coefficient);
    translate(oldBarycenter);
  }
}
