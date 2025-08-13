package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = factory.openSession();
            transaction = session.beginTransaction();
            session.save(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new RuntimeException("Cannot add phone", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Phone> query = builder.createQuery(Phone.class);
            Root<Phone> phoneRoot = query.from(Phone.class);

            Map<String, CriteriaBuilder.In<Object>> predicates = new HashMap<>();

            for (String key : List.of("color", "model", "maker", "countryManufactured")) {
                Optional.ofNullable(params.get(key))
                        .filter(values -> values.length > 0)
                        .ifPresent(values -> {
                            CriteriaBuilder.In<Object> predicate = builder.in(phoneRoot.get(key));
                            for (String value : values) {
                                predicate.value(value);
                            }
                            predicates.put(key, predicate);
                        });
            }

            Predicate[] predicatesArr = predicates.values()
                    .toArray(new Predicate[0]);
            query.where(builder.and(predicatesArr));

            return session.createQuery(query).getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Cannot find phones", e);
        }
    }
}
