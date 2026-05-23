package com.example.proect_lab123.domain.validators;


public interface Validator<T> {
    /**
     *
     * @param entity:T valideaza o entitate de tip T
     * @throws ValidationException daca ceva nu e cum ar trebui
     */
    void validate(T entity) throws ValidationException;
}