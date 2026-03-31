package org.example.simulation;

/**
 * Callback для відстеження прогресу виконання симуляції.
 */
public interface ProgressCallback {
    /**
     * Викликається для оновлення прогресу.
     * @param checkedVariants кількість перевірених варіантів виконання
     * @param currentStep поточний крок виконання
     * @param maxSteps максимальна кількість кроків
     */
    void onProgress(long checkedVariants, int currentStep, int maxSteps);
}
