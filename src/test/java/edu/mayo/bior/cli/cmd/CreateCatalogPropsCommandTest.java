package edu.mayo.bior.cli.cmd;

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import edu.mayo.cli.InvalidDataException;
import edu.mayo.cli.InvalidOptionArgValueException;

public class CreateCatalogPropsCommandTest {

	@Rule
    public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void testCommand() throws IOException, InterruptedException, InvalidOptionArgValueException, InvalidDataException {
		CreateCatalogPropsCommand cc = new CreateCatalogPropsCommand();
		cc.execute(null,null);
		
		//cc.createColumnPropsFile(null,null,null);
	}
}
