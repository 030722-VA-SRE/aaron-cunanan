package com.revature.persistence;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.revature.models.Spell;
import com.revature.models.Spell.SpellType;
import com.revature.util.ConnectionUtil;

public class SpellPostgres implements SpellDao {
	
	private static boolean getConnectionFromFile = true;
	
	@Override
	public List<Spell> getSpells() {
		List<Spell> spells = new ArrayList<>();
		String sql = "select * from Spells;";
		
		try (Connection c = getConnection()) {
			PreparedStatement ps = c.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			
			while (rs.next()) {
				spells.add(createSpellFromRecord(rs));
			}
		} catch (SQLException e) {
			// TODO Proper Handling
			e.printStackTrace();
		}
		return spells;
	}

	@Override
	public Spell getSpell(int id) {
		Spell spell = null;
		String sql = "select * from Spells where id = ?;";
		
		try (Connection c = getConnection()) {
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				spell = createSpellFromRecord(rs);
			}
		} catch (SQLException e) {
			// TODO Proper handling
			e.printStackTrace();
		}
		return spell;
	}

	@Override
	public int addSpell(Spell spell) {
		int genId = -1;
		String sql = "insert into Spells (name, description, price, stock, type_id, cast_fp_cost, "
		             + "charge_fp_cost, slots_used, int_requirement, fai_requirement, arc_requirement) "
		             + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) returning id;";
		             //         n  d  p  s  ti if hf su ir fr ar
		
		try (Connection c = getConnection()) {
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setString(1, spell.getName());
			ps.setString(2, spell.getDescription());
			ps.setInt(3, spell.getPrice());
			ps.setInt(4, spell.getStock());
			ps.setInt(5, spell.getType().ordinal());
			ps.setInt(6, spell.getFpCost().cast);
			ps.setInt(7, spell.getFpCost().charge);
			ps.setInt(8, spell.getSlotsUsed());
			ps.setInt(9, spell.getStatRequirement().intelligence);
			ps.setInt(10, spell.getStatRequirement().faith);
			ps.setInt(11, spell.getStatRequirement().arcane);
			ResultSet rs = ps.executeQuery();
			
			if (rs.next()) {
				genId = rs.getInt("id");
			}
		} catch (SQLException e) {
			// TODO Proper handling
			e.printStackTrace();
		}
		return genId;
	}

	@Override
	public boolean deleteSpell(int id) {
		String sql = "delete from Spells where id = ?;";
		
		try (Connection c = getConnection()) {
			PreparedStatement ps = c.prepareStatement(sql);
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			// TODO check that operation was successful
		} catch (SQLException e) {
			// TODO Proper handling
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean updateSpell(Spell spell) {
		// TODO Auto-generated method stub
		return false;
	}
	
	private Spell createSpellFromRecord(ResultSet rs) throws SQLException {
		Spell s = new Spell();
		s.setId(rs.getInt("id"));
		s.setName(rs.getString("name"));
		s.setDescription(rs.getString("description"));
		s.setPrice(rs.getInt("price"));
		s.setStock(rs.getInt("stock"));
		s.setType(SpellType.values()[rs.getInt("type_id")]);	// "cast" int to SpellType enum
		s.setFpCost(rs.getInt("cast_fp_cost"), rs.getInt("charge_fp_cost"));
		s.setSlotsUsed(rs.getInt("slots_used"));
		s.setStatRequirement(rs.getInt("int_requirement"),
		                     rs.getInt("fai_requirement"),
		                     rs.getInt("arc_requirement"));
		return s;
	}
	
	private Connection getConnection() throws SQLException {
		if (getConnectionFromFile) {
			try (Connection c = ConnectionUtil.getConnectionFromFile()) {
				return c;
			} catch (IOException e) {
				// TODO Proper handling
				e.printStackTrace();
			}
		}
		return ConnectionUtil.getConnectionFromEnv();
	}
}