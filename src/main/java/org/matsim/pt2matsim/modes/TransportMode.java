package org.matsim.pt2matsim.modes;

public final class TransportMode {

	public static final String car = "car";
	public static final String ride = "ride";
	public static final String bike = "bike";
	public static final String motorcycle = "motorcycle" ;
	public static final String truck = "truck" ;
	public static final String pt = "pt";
	public static final String drt = "drt" ;
	public static final String taxi = "taxi" ;
	public static final String walk = "walk";
	public static final String transit_walk = "transit_walk";
	public static final String train = "train";
	public static final String ship = "ship";
	public static final String airplane = "airplane";
	public static final String car_passenger = "car_passenger";
	public static final String bus = "bus";
	public static final String coach = "coach";
	public static final String pedestrian = "pedestrian";
	public static final String rail = "rail";
	public static final String subway = "subway";
	public static final String tram = "tram";
	


	@Deprecated // use non_network_walk
	public static final String access_walk = "non_network_walk" ;
	@Deprecated // use non_network_walk
	public static final String egress_walk = "non_network_walk" ;
	// (The directionality is not useful: what may be an egress_walk from the point of view of drt may be an access_walk from the point of view of pt.
	// kai, jun'19)
	
	// non_network_walk as access/egress to modes other than walk on the network was replaced by walk. - kn/gl-nov'19 

	public static final String non_network_walk = "non_network_walk" ;

	public static final String other = "other";

	private TransportMode() {
		// prevent creating instances of this class
	}


}
