package rega.genotype.ui.viruses.hiv;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import rega.genotype.FileFormatException;
import rega.genotype.ParameterProblemException;
import rega.genotype.ui.data.AbstractCsvGenerator;
import rega.genotype.ui.data.DefaultCsvGenerator;
import rega.genotype.ui.data.OrganismDefinition;
import rega.genotype.ui.data.SaxParser;
import rega.genotype.ui.forms.AbstractJobOverview;
import rega.genotype.ui.forms.DefaultJobOverview;
import rega.genotype.ui.forms.IDetailsForm;
import rega.genotype.ui.forms.details.DefaultPhylogeneticDetailsForm;
import rega.genotype.ui.forms.details.DefaultRecombinationDetailsForm;
import rega.genotype.ui.forms.details.DefaultSequenceAssignmentForm;
import rega.genotype.ui.forms.details.DefaultSignalDetailsForm;
import rega.genotype.ui.framework.GenotypeWindow;
import rega.genotype.ui.util.Genome;
import rega.genotype.viruses.hiv.HIVTool;

public class HivDefinition implements OrganismDefinition {
	private HivGenome genome = new HivGenome(this);

	public void startAnalysis(File jobDir) throws IOException, ParameterProblemException, FileFormatException {
		HIVTool hiv = new HIVTool(jobDir);
		hiv.analyze(jobDir.getAbsolutePath() + File.separatorChar + "sequences.fasta",
				jobDir.getAbsolutePath() + File.separatorChar + "result.xml");
	}

	public AbstractJobOverview getJobOverview(GenotypeWindow main) {
		return new DefaultJobOverview(main);
	}
	
	public String getOrganismDirectory() {
		return "/rega/genotype/ui/viruses/hiv/";
	}

	public AbstractCsvGenerator getCsvGenerator(PrintStream ps) {
		return new DefaultCsvGenerator(ps);
	}

	public Genome getGenome() {
		return genome;
	}

	public IDetailsForm getMainDetailsForm() {
		return new DefaultSequenceAssignmentForm();
	}

	public List<IDetailsForm> getSupportingDetailsforms(SaxParser p) {
		List<IDetailsForm> forms = new ArrayList<IDetailsForm>();
		
		if(p.elementExists("genotype_result.sequence.result[pure]") || p.elementExists("genotype_result.sequence.result[pure-puzzle]")) {
			DefaultPhylogeneticDetailsForm pdf = new DefaultPhylogeneticDetailsForm();
			forms.add(pdf);
		}
		if(p.elementExists("genotype_result.sequence.result[scan]") || p.elementExists("genotype_result.sequence.result[crfscan]")) {
			DefaultRecombinationDetailsForm rdf = new DefaultRecombinationDetailsForm();
			forms.add(rdf);
		}
		if(p.elementExists("genotype_result.sequence.result[pure-puzzle]")) {
			DefaultSignalDetailsForm sdf = new DefaultSignalDetailsForm();
			forms.add(sdf);
		}
		
		return forms;
	}
}