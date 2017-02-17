package org.matsim.pt2matsim.mapping;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Network;
import org.matsim.pt.transitSchedule.api.TransitLine;
import org.matsim.pt.transitSchedule.api.TransitRoute;
import org.matsim.pt.transitSchedule.api.TransitSchedule;
import org.matsim.pt2matsim.config.PublicTransitMappingConfigGroup;
import org.matsim.pt2matsim.gtfs.GtfsConverter;
import org.matsim.pt2matsim.gtfs.GtfsFeed;
import org.matsim.pt2matsim.gtfs.GtfsFeedImpl;
import org.matsim.pt2matsim.lib.RouteShape;
import org.matsim.pt2matsim.lib.ShapedSchedule;
import org.matsim.pt2matsim.lib.ShapedTransitSchedule;
import org.matsim.pt2matsim.mapping.networkRouter.ScheduleRoutersWithShapes;
import org.matsim.pt2matsim.plausibility.MappingAnalysis;
import org.matsim.pt2matsim.shp.Schedule2ShapeFile;
import org.matsim.pt2matsim.tools.NetworkTools;
import org.matsim.pt2matsim.tools.ScheduleTools;
import org.matsim.pt2matsim.tools.ShapeTools;
import org.matsim.pt2matsim.tools.debug.ExtractDebugSchedule;

import java.util.Collections;

/**
 * @author polettif
 */
public class PTMapperImplShapesTest {

	protected static Logger log = Logger.getLogger(PTMapperImplShapesTest.class);

	private String base = "test/analysis/";
	private String networkName = base + "network/addison.xml.gz";
	private String coordSys = "EPSG:2032";
	private String gtfsFolder = base + "addisoncounty-vt-us-gtfs/";
	private String sampleDay = GtfsConverter.ALL_SERVICE_IDS;

	private GtfsFeed gtfsFeed;
	private Network network;

	private Id<TransitLine> debugLineId = Id.create("TTSB/B_1438", TransitLine.class);
	private Id<TransitRoute> debugRouteId = Id.create("602798A4122B5456", TransitRoute.class);

	public static PublicTransitMappingConfigGroup createPTMConfig() {
		PublicTransitMappingConfigGroup config = new PublicTransitMappingConfigGroup();
		config.getModesToKeepOnCleanUp().add("car");
		PublicTransitMappingConfigGroup.LinkCandidateCreatorParams lccParamsBus = new PublicTransitMappingConfigGroup.LinkCandidateCreatorParams("bus");
		lccParamsBus.setNetworkModesStr("car,bus");
		lccParamsBus.setMaxNClosestLinks(16);
		lccParamsBus.setMaxLinkCandidateDistance(100);
		lccParamsBus.setLinkDistanceTolerance(1.1);
		config.addParameterSet(lccParamsBus);

		PublicTransitMappingConfigGroup.ModeRoutingAssignment mraBus = new PublicTransitMappingConfigGroup.ModeRoutingAssignment("bus");
		mraBus.setNetworkModesStr("car,bus");
		config.addParameterSet(mraBus);

		config.setNumOfThreads(8);

		return config;
	}

	@Before
	public void run() throws Exception {
		network = NetworkTools.readNetwork(networkName);

		gtfsFeed = new GtfsFeedImpl(gtfsFolder);
		GtfsConverter gtfsConverter = new GtfsConverter(gtfsFeed);
		gtfsConverter.convert(sampleDay, coordSys);

		gtfsConverter.getShapedTransitSchedule().getTransitRouteShapeReference().writeToFile(base + "output/shapeRef.csv");
		ScheduleTools.writeTransitSchedule(gtfsConverter.getSchedule(), base + "mts/unmapped_schedule.xml.gz");

		// debug shapes
//		ShapeTools.writeShapeFile(Collections.singleton(gtfsConverter.getShapes().get("26")), coordSys, base + "output/gtfsShapeDebug.shp");
	}

	private void runNormalMapping() {
		PublicTransitMappingConfigGroup config = createPTMConfig();

		TransitSchedule schedule = ScheduleTools.readTransitSchedule(base + "mts/unmapped_schedule.xml.gz");
		PTMapper ptMapper = new PTMapperImpl(config, schedule, network);
		ptMapper.run();

		NetworkTools.writeNetwork(network, base + "output/normal_network.xml.gz");
		ScheduleTools.writeTransitSchedule(ptMapper.getSchedule(), base + "output/normal_schedule.xml.gz");
	}

	private void runMappingWithShapes() {
		PublicTransitMappingConfigGroup config = createPTMConfig();
		ShapedTransitSchedule shapedSchedule = new ShapedSchedule(base + "mts/unmapped_schedule.xml.gz", base + "output/shapeRef.csv", gtfsFolder+"shapes.txt", coordSys);

		PTMapper ptMapper = new PTMapperImpl(config, shapedSchedule, network, new ScheduleRoutersWithShapes(config, shapedSchedule, network));
		ExtractDebugSchedule.run(shapedSchedule, debugLineId.toString(), debugRouteId.toString());
		ptMapper.run();

		NetworkTools.writeNetwork(network, base + "output/shapes_network.xml.gz");
		ScheduleTools.writeTransitSchedule(ptMapper.getSchedule(), base + "output/shapes_schedule.xml.gz");

	}

	@Test
	public void mappingAnalysisNormal() {
		runNormalMapping();

		ShapedTransitSchedule shapedSchedule = new ShapedSchedule(base + "output/normal_schedule.xml.gz", base + "output/shapeRef.csv", gtfsFolder+"shapes.txt", coordSys);

		MappingAnalysis analysis = new MappingAnalysis(shapedSchedule, NetworkTools.readNetwork(base + "output/normal_network.xml.gz"));

		analysis.run();
		analysis.writeQuantileDistancesCsv(base +"output/Normal_DistancesQuantile.csv");
		System.out.println("Q8585 normal: " + analysis.getQ8585());
		System.out.println("Length diff. normal: " + Math.sqrt(analysis.getAverageSquaredLengthRatio())*100 + " %");
	}

	@Test
	public void mappingAnalysisWithShapes() {
		runMappingWithShapes();

		ShapedTransitSchedule shapedSchedule = new ShapedSchedule(base + "output/shapes_schedule.xml.gz", base + "output/shapeRef.csv", gtfsFolder+"shapes.txt", coordSys);

		MappingAnalysis analysis = new MappingAnalysis(shapedSchedule, NetworkTools.readNetwork(base + "output/shapes_network.xml.gz"));

		analysis.run();
		analysis.writeQuantileDistancesCsv(base +"/output/Shapes_DistancesQuantile.csv");
		System.out.println("Q8585 with shapes: " + analysis.getQ8585());
		System.out.println("Length diff. shapes: " + Math.sqrt(analysis.getAverageSquaredLengthRatio())*100 + " %");

		/*
		PlausibilityCheck.run(
				base + "shape/output/shapes_schedule.xml.gz",
				base + "shape/output/shapes_network.xml.gz",
				"EPSG:2032",
				base + "shape/output/check/"
		);
		*/
	}

	@Test
	public void writeShapes() {
		Schedule2ShapeFile.run(coordSys, base + "output/shp/", base + "output/shapes_schedule.xml.gz", base + "output/shapes_network.xml.gz");
		ShapedTransitSchedule shapedSchedule = new ShapedSchedule(base + "output/shapes_schedule.xml.gz", base + "output/shapeRef.csv", gtfsFolder + "shapes.txt", coordSys);

		Id<RouteShape> shapeId = shapedSchedule.getTransitRouteShapeReference().getShapeId(debugLineId, debugRouteId);
		ShapeTools.writeShapeFile(Collections.singleton(shapedSchedule.getShapes().get(shapeId)), coordSys, base + "output/shp/gtfsShapes.shp");
	}


}