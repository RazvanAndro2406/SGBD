package com.example.proect_lab123.domain.validators;

import com.example.proect_lab123.domain.Actor;

import java.time.LocalDate;

public class ActorValidator implements Validator<Actor> {
    @Override
    public void validate(Actor entity) throws ValidationException {
        if(entity==null)
            throw new ValidationException("entity is null");
        if(entity.getBirthday()==null)
            throw new ValidationException("Birthday is null");
        if(entity.getName()==null || entity.getName().isEmpty())
            throw new ValidationException("Name cannot be null or empty");

    }
}
