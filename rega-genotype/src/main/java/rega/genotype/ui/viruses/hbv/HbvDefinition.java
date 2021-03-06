package rega.genotype.ui.viruses.hbv;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import rega.genotype.FileFormatException;
import rega.genotype.ParameterProblemException;
import rega.genotype.data.GenotypeResultParser;
import rega.genotype.ui.data.AbstractDataTableGenerator;
import rega.genotype.ui.data.DefaultTableGenerator;
import rega.genotype.ui.data.OrganismDefinition;
import rega.genotype.ui.forms.AbstractJobOverview;
import rega.genotype.ui.forms.DefaultJobOverview;
import rega.genotype.ui.forms.IDetailsForm;
import rega.genotype.ui.forms.details.DefaultPhylogeneticDetailsForm;
import rega.genotype.ui.forms.details.DefaultRecombinationDetailsForm;
import rega.genotype.ui.forms.details.DefaultSequenceAssignmentForm;
import rega.genotype.ui.forms.details.DefaultSignalDetailsForm;
import rega.genotype.ui.framework.GenotypeWindow;
import rega.genotype.ui.util.DataTable;
import rega.genotype.ui.util.Genome;
import rega.genotype.viruses.hbv.HBVTool;
import eu.webtoolkit.jwt.WString;

/**
 * HBV OrganismDefinition implementation
 * 
 * @author plibin0
 */
public class HbvDefinition implements OrganismDefinition {
	private Genome genome = new Genome(new HbvGenome(this));

	public AbstractDataTableGenerator getDataTableGenerator(AbstractJobOverview jobOverview, DataTable t)
			throws IOException {
		return new DefaultTableGenerator(jobOverview, t);
	}

	public Genome getGenome() {
		return genome;
	}

	public AbstractJobOverview getJobOverview(GenotypeWindow main) {
		return new DefaultJobOverview(main);
	}

	public IDetailsForm getMainDetailsForm() {
		return new DefaultSequenceAssignmentForm(2);
	}

	public String getProfileScanType(GenotypeResultParser p) {
		return "pure";
	}

	public String getOrganismDirectory() {
		return "/rega/genotype/ui/viruses/hbv/";
	}

	public String getOrganismName() {
		return "HBV";
	}

	public List<IDetailsForm> getSupportingDetailsforms(GenotypeResultParser p) {
		List<IDetailsForm> forms = new ArrayList<IDetailsForm>();

		WString m = new WString("Phylogenetic analysis with pure subtypes:");
		
		if (p.elementExists("/genotype_result/sequence/result[@id='pure']"))
			forms.add(new DefaultPhylogeneticDetailsForm("/genotype_result/sequence/result[@id='pure']", m, m, false));
		else if (p.elementExists("/genotype_result/sequence/result[@id='pure-puzzle']"))
			forms.add(new DefaultPhylogeneticDetailsForm("/genotype_result/sequence/result[@id='pure-puzzle']", m, m, false));

		m = new WString("Phylogenetic analysis with pure subtypes and CRFs:");

		if (p.elementExists("/genotype_result/sequence/result[@id='crf']"))
			forms.add(new DefaultPhylogeneticDetailsForm("/genotype_result/sequence/result[@id='crf']", m, m, false));
		
		String scan = "/genotype_result/sequence/result[@id='scan-pure']";
		if (p.elementExists(scan))
			forms.add(new DefaultRecombinationDetailsForm(scan, "pure", new WString("HBV Subtype Recombination Analysis")));
		
		String crfScan = "/genotype_result/sequence/result[@id='scan-crf']";
		if (p.elementExists(crfScan))
			forms.add(new DefaultRecombinationDetailsForm(crfScan, "crf", new WString("HBV CRF/Subtype Recombination Analysis")));

		if(p.elementExists("/genotype_result/sequence/result[@id='pure-puzzle']")) {
			forms.add(new DefaultSignalDetailsForm());
		}
		
		return forms;
	}

	public int getUpdateInterval() {
		return 5000;
	}

	public boolean haveDetailsNavigationForm() {
		return true;
	}

	public void startAnalysis(File jobDir) throws IOException,
			ParameterProblemException, FileFormatException {
		HBVTool hbv = new HBVTool(jobDir);
		hbv.analyze(jobDir.getAbsolutePath() + File.separatorChar + "sequences.fasta",
				jobDir.getAbsolutePath() + File.separatorChar + "result.xml");
	}

	public Genome getLargeGenome() {
		return getGenome();
	}
}
