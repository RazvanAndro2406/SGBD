package com.example.proect_lab123.domain.validators;

import com.example.proect_lab123.domain.Movie;

public class MovieValidator implements Validator<Movie> {
    @Override
    public void validate(Movie entity) throws ValidationException {
        if(entity==null)
            throw new ValidationException("entity is null");
        if(entity.getDuration()<=0)
            throw new ValidationException("Duration has to be positive");
        if(entity.getTitle().isEmpty())
            throw new ValidationException("Title cannot be null");
        if(entity.getGenre().isEmpty())
            throw new ValidationException("Genre cannot be null");
    }
}
