{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";

    flake-utils = {
      url = "github:numtide/flake-utils";
      inputs.nixpkgs.follows = "nixpkgs";
    };

    crow = {
      url = "github:I-Al-Istannen/crow";
      inputs.nixpkgs.follows = "nixpkgs";
    };
  };

  outputs = { nixpkgs, flake-utils, crow, ... }: flake-utils.lib.eachDefaultSystem (system:
    let
      pkgs = import nixpkgs {
        inherit system;

        overlays = [
          (final: prev: {
            crow-client = crow.packages.${prev.system}.client;
          })
        ];
      };
    in
    {
      devShells.default = pkgs.mkShell {
        packages = builtins.attrValues {
          inherit (pkgs) gradle jdk24 gcc14;
          inherit (pkgs) pre-commit google-java-format crow-client;
        };
      };
    });
}
