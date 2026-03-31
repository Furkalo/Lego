package org.example.simulation;

import org.example.model.ProgramSpecification;

import java.util.List;

/**
 * Інтерфейс симулятора/тестера набору блок-схем.
 *
 * Має підтримувати:
 * - запуск на одному тест-кейсі з перебором усіх виконань
 *   до певної максимальної кількості кроків;
 * - можливість достроково припинити перебір (через флаг cancel)
 *   і порахувати відсоток уже перевірених варіантів.
 */
public interface ProgramSimulator {

    /**
     * Проганяє програму, задану блок-схемами, на одному тесті,
     * перебираючи всі можливі міжпотокові виконання до maxSteps кроків.
     *
     * @param spec       специфікація програми
     * @param testCase   тест (вхід + очікуваний вихід)
     * @param maxSteps   максимум виконаних операцій у траєкторії (1..K)
     * @param cancelFlag одновимірний масив, де cancelFlag[0] може бути змінено
     *                   зовнішнім кодом у true для дострокової зупинки
     * @return список результатів для вже перевірених варіантів виконання
     */
    List<ExecutionResult> simulateAll(ProgramSpecification spec,
                                      TestCase testCase,
                                      int maxSteps,
                                      boolean[] cancelFlag);

    /**
     * @param checkedVariants кількість уже перевірених варіантів виконання
     * @param totalVariants   оцінка або точна кількість усіх можливих варіантів
     * @return відсоток перевірених варіантів у діапазоні 0..100
     */
    default double coveragePercent(long checkedVariants, long totalVariants) {
        if (totalVariants <= 0) return 0.0;
        return (checkedVariants * 100.0) / totalVariants;
    }
}

