#include <iostream> // TODO: Remove
#include <string>

void applyNonBlindWatermark(const std::string inputImagePath, const std::string outputImagePath, const std::string watermarkImagePath) {
	// Output information to console; remove if done testing
	std::cout << "Non-blind watermarking:" << std::endl;
	std::cout << "  Input:     " << inputImagePath << std::endl;
	std::cout << "  Output:    " << outputImagePath << std::endl;
	std::cout << "  Watermark: " << watermarkImagePath << std::endl << std::endl;

	// Read input image and watermark image from file
	// TODO

	// Convert watermark image to grayscale
	// TODO

	// Blend watermark image on input image
	// TODO

	// Write resulting image into output file
	// TODO
}

void applyBlindWatermark(const std::string inputImagePath, const std::string outputImagePath, const std::string message, const std::string alphabet, const unsigned int bitsUsed) {
	// Output information to console; remove if done testing
	std::cout << "Blind watermarking:" << std::endl;
	std::cout << "  Input:     " << inputImagePath << std::endl;
	std::cout << "  Output:    " << outputImagePath << std::endl;
	std::cout << "  Message:   " << message << std::endl;
	std::cout << "  Alphabet:  " << alphabet << std::endl;
	std::cout << "  Bits used: " << bitsUsed << std::endl << std::endl;

	// Read input image from file
	// TODO

	// Convert message into binary code based on the alphabet supplied
	// TODO

	// Apply the binary code to the number of least significant bits supplied
	// TODO

	// Write resulting image into output file
	// TODO
}

std::string readBlindWatermark(const std::string imagePath, const std::string alphabet, const unsigned int bitsUsed) {
	// Output information to console; remove if done testing
	std::cout << "Reading blind watermark:" << std::endl;
	std::cout << "  Image:     " << imagePath << std::endl;
	std::cout << "  Alphabet:  " << alphabet << std::endl;
	std::cout << "  Bits used: " << bitsUsed << std::endl << std::endl;

	// Read input image from file
	// TODO

	// Read binary code from image based on the number of least significant bits used
	// TODO

	// Convert binary code to a message based on the alphabet supplied
	std::string message = "";
	// TODO

	// Return the message read from the image
	return message;
}

int main() {
	// Non-blind watermarking
	applyNonBlindWatermark("image.png", "nonblind.png", "watermark.png");

	// Blind watermarking
	applyBlindWatermark("image.jpg", "blind.png", "This is copyrighted material!", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!? ", 2);

	// Read blind watermark
	std::cout << "Watermark:" << std::endl;
	std::cout << readBlindWatermark("blind.png", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!? ", 2) << std::endl;

	return 0;
}
