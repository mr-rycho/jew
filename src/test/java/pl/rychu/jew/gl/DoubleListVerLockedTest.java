package pl.rychu.jew.gl;

import junitparams.JUnitParamsRunner;

import org.junit.runner.RunWith;



@RunWith(JUnitParamsRunner.class)
public class DoubleListVerLockedTest extends DoubleListVerTest {

	@Override
	protected DoubleListVer<Integer> createList() {
		return DoubleListVerLocked.create(super.createList());
	}

}
