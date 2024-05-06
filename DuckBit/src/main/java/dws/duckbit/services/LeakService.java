package dws.duckbit.services;

import dws.duckbit.entities.Combo;
import dws.duckbit.repositories.LeaksRepository;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import dws.duckbit.entities.Leak;
import org.springframework.web.multipart.MultipartFile;


@Service
public class LeakService
{
	private final Path LEAKS_FOLDER = Paths.get("files/leaks");

	private final LeaksRepository leaksRepository;
	private int id = 1;
	public LeakService(LeaksRepository leaksRepository){
		this.leaksRepository = leaksRepository;
	}

// ---------- CONSTRUCTOR ---------- //

	public Leak createLeak(String enterprise, String date, String filename)
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date fechaDate = sdf.parse(date);

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(fechaDate);
			Leak l = new Leak(enterprise, calendar, filename);
			this.id++;
			save(l);
			return l;
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
		return null;
	}

// ---------- GET ---------- //

	public int getNextId()
	{
		return this.id;
	}

	public long getSize()
	{
		return this.leaksRepository.count();
	}

	public Optional<Leak> findByID(long id)
	{
		return this.leaksRepository.findById(id);
	}

	public List<Leak> findAll()
	{
		return this.leaksRepository.findAll();
	}
	
	public List<Leak> findByCombo(Combo c)
	{
		return this.leaksRepository.findLeaksByCombos(c);
	}

	public boolean existsLeakByFilename(String filename)
	{
		return this.leaksRepository.existsLeakByFilename(filename);
	}

// ---------- ADD AND CREATE ---------- //

	public int upload(MultipartFile leak, String enterprise, LeakService leaksDB, String date) throws IOException
	{
		String REGEX_PATTERN = "^[A-Za-z.]{1,255}$";
		String REGEX_DATE_PATTERN = "^\\d{4}-\\d{2}-\\d{2}$";
		String filename = leak.getOriginalFilename();
		if (enterprise.length() > 255 || enterprise.isEmpty())
			return 1;
		if(filename == null || !(filename.matches(REGEX_PATTERN)) || leaksDB.existsLeakByFilename(filename))
			return 2;
		if (!(date.matches(REGEX_DATE_PATTERN)) || Integer.parseInt(date.toString().split("-")[0]) > 9990)
			return 3;
		return 0;
	}

	public void save(Leak l)
	{
		this.leaksRepository.save(l);
	}

// ---------- DELETE AND REMOVE ---------- //

	public void delete(Leak l) throws IOException
	{
		this.leaksRepository.delete(l);
		Files.createDirectories(this.LEAKS_FOLDER);
		String nameFile = l.getId() + ".txt";
		Path leakPath = this.LEAKS_FOLDER.resolve(nameFile);
		File leak = leakPath.toFile();
		if (leak.exists())
		{
			leak.delete();
		}
	}
}
