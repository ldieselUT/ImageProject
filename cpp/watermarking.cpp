#include <iostream> // TODO: Remove after testing is done
#include <string>
#include <algorithm>

#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>


void displayImage(const cv::Mat &image, const std::string &title) {
	cv::namedWindow(title, cv::WINDOW_AUTOSIZE );
	cv::imshow(title, image);
	cv::waitKey(0);
}


cv::Mat readImageFromFile(const std::string &imagePath, const int &flags) {
	cv::Mat image;
	image = cv::imread(imagePath, flags);
	if (!image.data) {
		std::cout << "Error: Image could not be read from " << imagePath << "." << std::endl;
		throw;
	}
	return image;
}


void outputImageToFile(const std::string &imagePath, const cv::Mat &image) {
	imwrite(imagePath, image);
}


void applyNonBlindWatermark(const std::string &inputImagePath, const std::string &outputImagePath, const std::string &watermarkImagePath, const double &watermarkWeight) {
	// Output information to console; TODO: remove after testing is done
	std::cout << "Non-blind watermarking:" << std::endl;
	std::cout << "  Input:     " << inputImagePath << std::endl;
	std::cout << "  Output:    " << outputImagePath << std::endl;
	std::cout << "  Watermark: " << watermarkImagePath << std::endl << std::endl;

	// Read input image and watermark images from the corresponding input files
	cv::Mat inputImage = readImageFromFile(inputImagePath, CV_LOAD_IMAGE_COLOR);
	cv::Mat watermarkImage = readImageFromFile(watermarkImagePath, CV_LOAD_IMAGE_COLOR);

	// Display the input and watermark images; TODO: remove after testing is done
	displayImage(inputImage, "Non-blind watermarking, input image");
	displayImage(watermarkImage, "Non-blind watermarking, watermark image");

	// Blend watermark image on input image
	cv::Mat outputImage;
	cv::addWeighted(inputImage, 1.0, watermarkImage, watermarkWeight, 0.0, outputImage);

	// Display the watermarked image; TODO: remove after testing is done
	displayImage(outputImage, "Non-blind watermarking, result image");

	// Write resulting image into output file
	outputImageToFile(outputImagePath, outputImage);
}


std::string getBinary(const char &character, const std::string &alphabet, const unsigned int &binaryLength) {
	const char* charPointer = std::strchr(alphabet.c_str(), character);
	if (charPointer == NULL) {
		std::cout << "Error: Character could not be found in alphabet." << std::endl;
		throw;
	}
	unsigned int position = charPointer - alphabet.c_str();
	std::string binary;
	do {
		binary.push_back('0' + (position & 1));
	} while (position >>= 1);
	while (binary.length() < binaryLength) {
		binary.push_back('0');
	}
	std::reverse(binary.begin(), binary.end());
	return binary;
}


void applyBlindWatermark(const std::string &inputImagePath, const std::string &outputImagePath, const std::string &message, const std::string &alphabet, const unsigned int &bitsUsed) {
	// Output information to console; TODO: remove after testing is done
	std::cout << "Blind watermarking:" << std::endl;
	std::cout << "  Input:     " << inputImagePath << std::endl;
	std::cout << "  Output:    " << outputImagePath << std::endl;
	std::cout << "  Message:   " << message << std::endl;
	std::cout << "  Alphabet:  " << alphabet << std::endl;
	std::cout << "  Bits used: " << bitsUsed << std::endl << std::endl;

	// Read input image from file
	cv::Mat inputImage = readImageFromFile(inputImagePath, CV_LOAD_IMAGE_COLOR);

	// Create alphabet associations with binary code
	unsigned int binaryLength = 1;
	unsigned int binaryLimit = 2;
	while (alphabet.length() > binaryLimit) {
		binaryLength += 1;
		binaryLimit *= 2;
	}

	// Convert message into binary code based on the alphabet supplied
	std::string binaryMessage = "";
	for (const char &character: message) {
		binaryMessage += getBinary(character, alphabet, binaryLength);
	}

	// Apply the binary code to the number of least significant bits supplied
	cv::Mat outputImage = inputImage.clone();
	// TODO

	// Write resulting image into output file
	outputImageToFile(outputImagePath, outputImage);
}


std::string readBlindWatermark(const std::string &imagePath, const std::string &alphabet, const unsigned int &bitsUsed) {
	// Output information to console; TODO: remove after testing is done
	std::cout << "Reading blind watermark:" << std::endl;
	std::cout << "  Image:     " << imagePath << std::endl;
	std::cout << "  Alphabet:  " << alphabet << std::endl;
	std::cout << "  Bits used: " << bitsUsed << std::endl << std::endl;

	// Read input image from file
	cv::Mat image = readImageFromFile(imagePath, CV_LOAD_IMAGE_COLOR);

	// Read binary code from image based on the number of least significant bits used
	// TODO

	// Convert binary code to a message based on the alphabet supplied
	std::string message = "";
	// TODO

	// Return the message read from the image
	return message;
}


// TODO: Remove after testing is done
int main() {
	// Non-blind watermarking
	applyNonBlindWatermark("image.png", "nonblind.png", "watermark.png", 0.35);

	// Blind watermarking
	applyBlindWatermark("image.png", "blind.png", "This is copyrighted material!", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!? ", 2);

	// Read blind watermark
	std::string watermark = readBlindWatermark("blind.png", "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789.,!? ", 2);
	std::cout << "Watermark:" << watermark << std::endl;

	return 0;
}
