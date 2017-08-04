package pl.rychu.jew.gui.mapper;

import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;


/**
 * Created on 04.08.2017.
 */
public class MapperTest {

	@Test
	public void testGetSet() {
		// given
		Mapper mapper = Mapper.create(16);

		// when
		mapper.addF(50);
		mapper.addF(60);
		mapper.addB(40);
		mapper.addB(30);
		mapper.addB(20);
		mapper.addB(10);
		mapper.addB(0);

		// then
		for (int index = -5; index < 2; index++) {
			assertThat(mapper.get(index)).isEqualTo((index + 5) * 10);
		}
	}

	@Test
	public void testBinarySearch() {
		// given
		Mapper mapper = Mapper.create(16);

		// when
		mapper.addF(50);
		mapper.addF(60);
		mapper.addB(40);
		mapper.addB(30);
		mapper.addB(20);
		mapper.addB(10);
		mapper.addB(0);

		// then
		assertThat(mapper.getInv(-5)).isEqualTo(-1);
		assertThat(mapper.getInv(0)).isEqualTo(0);
		assertThat(mapper.getInv(5)).isEqualTo(-2);
		assertThat(mapper.getInv(10)).isEqualTo(1);
		assertThat(mapper.getInv(15)).isEqualTo(-3);
		assertThat(mapper.getInv(20)).isEqualTo(2);
		assertThat(mapper.getInv(25)).isEqualTo(-4);
		assertThat(mapper.getInv(30)).isEqualTo(3);
		assertThat(mapper.getInv(35)).isEqualTo(-5);
		assertThat(mapper.getInv(40)).isEqualTo(4);
		assertThat(mapper.getInv(45)).isEqualTo(-6);
		assertThat(mapper.getInv(50)).isEqualTo(5);
		assertThat(mapper.getInv(55)).isEqualTo(-7);
		assertThat(mapper.getInv(60)).isEqualTo(6);
		assertThat(mapper.getInv(65)).isEqualTo(-8);
	}

	@Test
	public void testBinarySearchForward() {
		// given
		Mapper mapper = Mapper.create(16);

		// when
		mapper.addF(10);
		mapper.addF(20);
		mapper.addF(30);

		// then
		assertThat(mapper.getInv(5)).isEqualTo(-1);
		assertThat(mapper.getInv(10)).isEqualTo(0);
		assertThat(mapper.getInv(15)).isEqualTo(-2);
		assertThat(mapper.getInv(20)).isEqualTo(1);
		assertThat(mapper.getInv(25)).isEqualTo(-3);
		assertThat(mapper.getInv(30)).isEqualTo(2);
		assertThat(mapper.getInv(35)).isEqualTo(-4);
	}

	@Test
	public void testBinarySearchBackward() {
		// given
		Mapper mapper = Mapper.create(16);

		// when
		mapper.addB(30);
		mapper.addB(20);
		mapper.addB(10);

		// then
		assertThat(mapper.getInv(5)).isEqualTo(-1);
		assertThat(mapper.getInv(10)).isEqualTo(0);
		assertThat(mapper.getInv(15)).isEqualTo(-2);
		assertThat(mapper.getInv(20)).isEqualTo(1);
		assertThat(mapper.getInv(25)).isEqualTo(-3);
		assertThat(mapper.getInv(30)).isEqualTo(2);
		assertThat(mapper.getInv(35)).isEqualTo(-4);
	}

}