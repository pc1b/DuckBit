package dws.duckbit.services;

import java.sql.ResultSet; 
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import dws.duckbit.entities.Combo;
  
public class ComboMapper implements RowMapper<Combo>{ 
  
    @SuppressWarnings("null")
    @Override
    public Combo mapRow(ResultSet rs, int map) throws SQLException { 
  
        Combo combo = new Combo(); 
        combo.setId(rs.getInt("ID")); 
        combo.setName(rs.getString("NAME")); 
        combo.setDescription(rs.getString("DESCRIPTION"));
        combo.setPrice(rs.getInt("PRICE"));  
        return combo; 
    } 
} 

